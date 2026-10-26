package com.gdavidpb.tuindice.base.presentation.statemachine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.logging.MachineLog
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.utils.extension.eventName
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

abstract class StateMachineViewModel<S : ViewState, A : ViewAction, E : ViewEffect>(
	private val name: String,
	initialState: S,
	initialAction: A? = null,
	private val dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : ViewModel() {
	private val effectChannel = Channel<E>(Channel.UNLIMITED)
	private val inputChannel = Channel<MachineInput<A>>(Channel.UNLIMITED)
	private val viewState = MutableStateFlow(initialState)
	private var lastPublishedState: String? = null
	private var activeTransitionEmits: Set<KClass<*>>? = null

	// Numbers the machine's jobs in the debug log, so a job's end can be told from another's.
	private var launchedJobs = 0

	protected abstract val eventPublisher: EventPublisher

	protected abstract val screenMachine: ScreenMachine<S, E>

	// Public on purpose: the machine is introspectable data — diagram export and
	// transition-table validation tooling read it from here.
	val machine: MachineDefinition<S> by lazy { screenMachine.define(host = machineHost()) }

	val effect = effectChannel.receiveAsFlow()

	val state = viewState
		.onStart {
			MachineLog.write(name) { "start in ${viewState.value.logText()}" }
			eventPublisher.publish(AppEvent.ScreenView(source = name))
			startMachineLoop()
			if (initialAction != null) sendAction(initialAction)
		}
		.onEach(::publishState)
		.flowOn(dispatchers.default)
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.Lazily,
			initialValue = initialState
		)

	protected val currentState: S
		get() = viewState.value

	protected fun sendAction(viewAction: A) {
		inputChannel.trySend(
			MachineInput(
				event = viewAction,
				action = viewAction
			)
		)
	}

	protected fun processInternalEvent(event: Any) {
		inputChannel.trySend(
			MachineInput(
				event = event,
				action = null
			)
		)
	}

	// G as data: effects may only flow while a transition is being applied, and only
	// those the active row declared in `emits`. Anything else is a table bug — fail loud.
	protected fun sendEffect(viewEffect: E) {
		val declared = checkNotNull(activeTransitionEmits) {
			"Effect ${viewEffect.eventName()} emitted outside a transition; " +
				"effects must be outputs of a table row"
		}

		check(declared.any { effect -> effect.isInstance(viewEffect) }) {
			"Effect ${viewEffect.eventName()} is not declared by the active transition"
		}

		val event = AppEvent.Effect(
			source = name,
			effect = viewEffect.eventName()
		)

		MachineLog.write(name) { "effect ${viewEffect.logText()}" }
		eventPublisher.publish(event)
		effectChannel.trySend(viewEffect)
	}

	protected fun launchMachineJob(block: suspend CoroutineScope.() -> Unit): Job {
		val job = viewModelScope.launch(dispatchers.default, block = block)

		if (MachineLog.sink != null) {
			val number = ++launchedJobs

			MachineLog.write(name) { "job $number started in ${viewState.value.logName()}" }
			job.invokeOnCompletion { cause -> MachineLog.write(name) { "job $number ${jobEnd(cause)}" } }
		}

		return job
	}

	override fun onCleared() {
		MachineLog.write(name) { "cleared in ${viewState.value.logName()}" }
		super.onCleared()
	}

	private fun machineHost(): MachineHost<E> {
		return object : MachineHost<E> {
			override fun sendEffect(effect: E) =
				this@StateMachineViewModel.sendEffect(effect)

			override fun processInternalEvent(event: Any) =
				this@StateMachineViewModel.processInternalEvent(event)

			override fun launchMachineJob(block: suspend CoroutineScope.() -> Unit): Job =
				this@StateMachineViewModel.launchMachineJob(block)
		}
	}

	private fun startMachineLoop() {
		viewModelScope.launch(dispatchers.default) {
			// Single consumer over a FIFO channel: transition resolution and state
			// application are serialized here, which is what makes them atomic.
			for (input in inputChannel) {
				if (input.action != null) {
					eventPublisher.publish(
						AppEvent.Action(
							source = name,
							action = input.action.eventName()
						)
					)
				}

				val fromState = viewState.value
				val transition = machine.resolve(fromState, input.event)
				val inputKind = if (input.action != null) "action" else "event"

				if (transition == null) {
					MachineLog.write(name) { "rejected $inputKind ${input.event.logText()} in ${fromState.logName()}" }
					eventPublisher.publish(
						AppEvent.InvalidTransition(
							source = name,
							from = fromState.eventName(),
							event = input.event.eventName()
						)
					)
				} else {
					activeTransitionEmits = transition.emits

					try {
						val toState = runCatching {
							machine.apply(
								transition = transition,
								state = fromState,
								event = input.event
							)
						}.onFailure { failure ->
							MachineLog.write(name) {
								"failed $inputKind ${input.event.logText()} in ${fromState.logName()}: $failure"
							}
						}.getOrThrow()

						viewState.value = toState

						MachineLog.write(name) {
							"${fromState.logName()} + $inputKind ${input.event.logText()} -> ${toState.logName()}"
						}
						if (toState != fromState) MachineLog.write(name) { "state ${toState.logText()}" }

						eventPublisher.publish(
							AppEvent.Transition(
								source = name,
								from = fromState.eventName(),
								event = input.event.eventName(),
								to = toState.eventName()
							)
						)
					} finally {
						activeTransitionEmits = null
					}
				}
			}
		}
	}

	private fun publishState(state: S) {
		val stateName = state.eventName()
		if (lastPublishedState == stateName) return

		lastPublishedState = stateName

		val event = AppEvent.State(
			source = name,
			state = stateName
		)

		eventPublisher.publish(event)
	}
}

// The debug log names a state by its class, as the code does; the analytics events use their own lower-case name.
private fun Any.logName(): String = this::class.simpleName ?: toString()

// An action or event with its data when it has any; one without a data class prints its class, not its address.
private fun Any.logText(): String {
	val text = toString()

	return if (text.endsWith("@${hashCode().toUInt().toString(radix = 16)}")) logName() else text
}

private fun jobEnd(cause: Throwable?): String = when (cause) {
	null -> "done"
	is CancellationException -> "cancelled"
	else -> "failed: $cause"
}

private data class MachineInput<A>(
	val event: Any,
	val action: A?
)
