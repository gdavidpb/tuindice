package com.gdavidpb.tuindice.assistant.presentation.resource

// The texts of an intent card the service did not take: a confirmation that did not go through, and a dismissal that
// did not reach the service. Their own port keeps the chat texts small, as the part texts do.
interface AssistantChatIntentTexts {
	// A confirmation that did not go through: the card is back on its buttons for a new try.
	suspend fun confirmFailed(): String

	// A dismissal that did not reach the service: the proposal stays open.
	suspend fun dismissFailed(): String
}
