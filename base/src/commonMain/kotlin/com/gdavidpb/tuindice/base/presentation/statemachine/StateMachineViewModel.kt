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
import kotlinx.coroutines.CompletableDeferred
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
import ru.nsk.kstatemachine.event.Event
import ru.nsk.kstatemachine.statemachine.BuildingStateMachine
import ru.nsk.kstatemachine.statemachine.ProcessingResult
import ru.nsk.kstatemachine.statemachine.StateMachine
import ru.nsk.kstatemachine.statemachine.createStdLibStateMachine

abstract class StateMachineViewModel<S : ViewState, A : ViewAction, E : ViewEffect>(
	private val name: String,
	initialState: S,
	initialAction: A? = null,
	private val dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : ViewModel() {
	private val effectChannel = Channel<E>(Channel.UNLIMITED)
	private val inputChannel = Channel<MachineInput<A>>(Channel.UNLIMITED)
	private val viewState = MutableStateFlow(initialState)
	private val machineRef = CompletableDeferred<StateMachine>()
	private var lastPublishedState: String? = null

	protected abstract val eventPublisher: EventPublisher

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

	protected abstract suspend fun createMachine(): StateMachine

	protected abstract fun toMachineEvent(action: A): Event

	// StdLib abstraction: the machine has no coroutine machinery of its own, so every
	// processEvent call (and the listeners it triggers) is confined to the single
	// machine-loop coroutine. That serialization is what makes transitions atomic.
	protected fun buildMachine(
		init: suspend BuildingStateMachine.() -> Unit
	): StateMachine {
		return createStdLibStateMachine(
			name = name,
			init = init
		)
	}

	protected val currentState: S
		get() = viewState.value

	protected fun sendAction(viewAction: A) {
		inputChannel.trySend(
			MachineInput(
				event = toMachineEvent(viewAction),
				action = viewAction
			)
		)
	}

	protected fun processInternalEvent(event: Event) {
		inputChannel.trySend(
			MachineInput(
				event = event,
				action = null
			)
		)
	}

	protected fun updateState(transform: (S) -> S) {
		viewState.value = transform(viewState.value)
	}

	protected fun sendEffect(viewEffect: E) {
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

	// Public on purpose: the machine is introspectable data — diagram export and
	// transition-table validation tooling read it from here.
	suspend fun awaitMachine(): StateMachine {
		return machineRef.await()
	}

	private fun startMachineLoop() {
		viewModelScope.launch(dispatchers.default) {
			val machine = createMachine()

			machineRef.complete(machine)

			for (input in inputChannel) {
				if (input.action != null) {
					eventPublisher.publish(
						AppEvent.Action(
							source = name,
							action = input.action.eventName()
						)
					)
				}

				val from = currentState.eventName()
				val result = machine.processEvent(input.event)

				when (result) {
					ProcessingResult.PROCESSED ->
						eventPublisher.publish(
							AppEvent.Transition(
								source = name,
								from = from,
								event = input.event.eventName(),
								to = currentState.eventName()
							)
						)

					ProcessingResult.IGNORED ->
						eventPublisher.publish(
							AppEvent.InvalidTransition(
								source = name,
								from = from,
								event = input.event.eventName()
							)
						)

					else -> Unit
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
	val event: Event,
	val action: A?
)
