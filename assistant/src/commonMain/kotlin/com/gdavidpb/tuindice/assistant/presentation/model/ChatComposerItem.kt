package com.gdavidpb.tuindice.assistant.presentation.model

// The text field, the send button and the photo button. [isEnabled] is false in guided mode and while a restriction
// holds; [isPhotoEnabled] is false as well while a turn or a photo is being read. While a turn can be stopped,
// [isStoppable] puts the stop button where the send button is. [textRevision] moves when the chat replaces the text
// (the send clears it, a refused send gives it back), so the field adopts it instead of taking it for an echo.
data class ChatComposerItem(
	val text: String,
	val isEnabled: Boolean,
	val isSendEnabled: Boolean,
	val isPhotoEnabled: Boolean = false,
	val isStoppable: Boolean = false,
	val textRevision: Int = 0
)
