package com.gdavidpb.tuindice.assistant.presentation.model

// The text field and the send button. [isEnabled] is false in guided mode and while a restriction holds.
data class ChatComposerItem(
	val text: String,
	val isEnabled: Boolean,
	val isSendEnabled: Boolean
)
