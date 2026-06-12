package com.gdavidpb.tuindice.base.presentation.statemachine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.utils.extension.eventName
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
	private val initialStateClass = initialState::class
	private val effectChannel = Channel<E>(Channel.UNLIMITED)
	private val inputChannel = Channel<MachineInput<A>>(Channel.UNLIMITED)
	private val viewState = MutableStateFlow(initialState)
	private var lastPublishedState: String? = null
	private var activeTransitionEmits: Set<KClass<*>>? = null

	protected abstract val eventPublisher: EventPublisher

	// Public on purpose: the machine is introspectable data — diagram export and
	// transition-table validation tooling read it from here.
	val machine: MachineDefinition<S> by lazy { defineMachine() }

	val effect = effectChannel.receiveAsFlow()

	val state = viewState
		.onStart {
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

	protected abstract fun defineMachine(): MachineDefinition<S>

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

		eventPublisher.publish(event)
		effectChannel.trySend(viewEffect)
	}

	protected fun launchMachineJob(block: suspend CoroutineScope.() -> Unit): Job {
		return viewModelScope.launch(dispatchers.default, block = block)
	}

	protected fun machineHost(): MachineHost<E> {
		return object : MachineHost<E> {
			override fun sendEffect(effect: E) =
				this@StateMachineViewModel.sendEffect(effect)

			override fun processInternalEvent(event: Any) =
				this@StateMachineViewModel.processInternalEvent(event)

			override fun launchMachineJob(block: suspend CoroutineScope.() -> Unit): Job =
				this@StateMachineViewModel.launchMachineJob(block)
		}
	}

	fun exportMachineToMermaid(): String {
		return machine.exportToMermaid(
			machineName = name,
			initialState = initialStateClass
		)
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

				if (transition == null) {
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
						val toState = machine.apply(
							transition = transition,
							state = fromState,
							event = input.event
						)

						viewState.value = toState

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

private data class MachineInput<A>(
	val event: Any,
	val action: A?
)
