package com.gdavidpb.tuindice.assistant.domain.model

import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage

/** One entry of the conversation: a message of the user, or a message of the assistant with the turn that wrote it. */
sealed interface AssistantConversationItem {
	data class User(val message: AssistantUserMessage) : AssistantConversationItem

	data class Agent(
		val message: AssistantMessage,
		val turnId: String
	) : AssistantConversationItem
}
