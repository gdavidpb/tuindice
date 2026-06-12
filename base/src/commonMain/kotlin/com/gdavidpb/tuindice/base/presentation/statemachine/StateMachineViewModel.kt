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

				when (val result = machine.process(fromState, input.event)) {
					is TransitionResult.Transitioned -> {
						viewState.value = result.toState

						eventPublisher.publish(
							AppEvent.Transition(
								source = name,
								from = fromState.eventName(),
								event = input.event.eventName(),
								to = result.toState.eventName()
							)
						)
					}

					is TransitionResult.Rejected ->
						eventPublisher.publish(
							AppEvent.InvalidTransition(
								source = name,
								from = fromState.eventName(),
								event = input.event.eventName()
							)
						)
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
