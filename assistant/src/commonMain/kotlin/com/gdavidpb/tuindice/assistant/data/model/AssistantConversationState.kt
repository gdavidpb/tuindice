package com.gdavidpb.tuindice.assistant.data.model

import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserMessage
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage

/**
 * The active conversation as its turns have built it.
 * It holds what the user sent, the messages of the assistant with the turn that wrote each one,
 * the open intents and the outcome of the resolved ones.
 */
data class AssistantConversationState(
	val conversationId: String? = null,
	val userMessages: List<AssistantUserMessage> = emptyList(),
	val messages: List<AssistantMessage> = emptyList(),
	val messageTurnIds: Map<String, String> = emptyMap(),
	val turns: Map<String, AssistantTurnState> = emptyMap(),
	val lastTurnId: String? = null,
	val openIntents: Map<String, AssistantOpenIntent> = emptyMap(),
	val resolvedIntents: Map<String, OutcomeKind> = emptyMap()
)
