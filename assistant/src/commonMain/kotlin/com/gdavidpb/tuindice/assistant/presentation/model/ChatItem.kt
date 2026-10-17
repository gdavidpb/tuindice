package com.gdavidpb.tuindice.assistant.presentation.model

// One entry of the conversation.
sealed interface ChatItem {
	val id: String

	// The student's bubble, on the right. [isFailed] when its turn could not start or failed.
	data class User(
		override val id: String,
		val text: String,
		val isFailed: Boolean
	) : ChatItem

	// The agent's message, full width and without a bubble, with its actions under it.
	data class Agent(
		override val id: String,
		val parts: List<ChatPartItem>,
		// The student's thumbs on it: true useful, false not useful, null not rated.
		val useful: Boolean?,
		// The plain text of the message, for Copy.
		val copyText: String
	) : ChatItem
}
