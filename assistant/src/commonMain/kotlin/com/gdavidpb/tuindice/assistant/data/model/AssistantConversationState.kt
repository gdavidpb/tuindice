package com.gdavidpb.tuindice.assistant.data.model

import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage

/**
 * The active conversation as the events of its turns have built it: its messages, its turns and the open intents.
 */
data class AssistantConversationState(
	val conversationId: String? = null,
	val messages: List<AssistantMessage> = emptyList(),
	val turns: Map<String, AssistantTurnState> = emptyMap(),
	val openIntents: Map<String, AssistantOpenIntent> = emptyMap()
)
