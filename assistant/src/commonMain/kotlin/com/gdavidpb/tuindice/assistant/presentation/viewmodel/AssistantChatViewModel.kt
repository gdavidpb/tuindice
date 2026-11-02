package com.gdavidpb.tuindice.assistant.presentation.viewmodel

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatMachine
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel

// The chat's screen API. Every action of the chat enters the table through [dispatch]; the route opens the chat
// with Open, so the view model has no initial action of its own.
class AssistantChatViewModel(
	override val screenMachine: AssistantChatMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<AssistantChat.State, AssistantChat.Action, AssistantChat.Effect>(
	name = "assistant_chat",
	initialState = screenMachine.initialState(),
	dispatchers = dispatchers
) {
	fun dispatch(action: AssistantChat.Action) {
		sendAction(action)
	}
}
