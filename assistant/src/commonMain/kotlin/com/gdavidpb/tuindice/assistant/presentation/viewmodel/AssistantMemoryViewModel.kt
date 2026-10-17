package com.gdavidpb.tuindice.assistant.presentation.viewmodel

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantMemory
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantMemoryMachine
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel

// The memory screen's API. The first load is its initial action, so the screen starts loading when it is observed.
class AssistantMemoryViewModel(
	override val screenMachine: AssistantMemoryMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<AssistantMemory.State, AssistantMemory.Action, AssistantMemory.Effect>(
	name = "assistant_memory",
	initialState = screenMachine.initialState(),
	initialAction = AssistantMemory.Action.Load,
	dispatchers = dispatchers
) {
	fun dispatch(action: AssistantMemory.Action) {
		sendAction(action)
	}
}
