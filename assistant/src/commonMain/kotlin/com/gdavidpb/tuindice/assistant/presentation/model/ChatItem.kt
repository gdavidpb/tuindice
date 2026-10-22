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
		// The student's report on it was saved: its flag shows filled and cannot be tapped again.
		val reported: Boolean,
		// The plain text of the message, for Copy.
		val copyText: String
	) : ChatItem
}

// Whether the item is an agent's message that has an intent card, in any status: the message is shown up to its card,
// with the card's buttons while it is open or running, and up to its outcome once it is resolved
// (AssistantIntentCardView).
internal val ChatItem.hasIntentCard: Boolean
	get() = this is ChatItem.Agent && parts.any { it is ChatPartItem.IntentCard }
