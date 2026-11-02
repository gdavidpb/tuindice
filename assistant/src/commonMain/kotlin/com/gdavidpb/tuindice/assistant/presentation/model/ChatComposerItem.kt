package com.gdavidpb.tuindice.assistant.presentation.model

// The text field, the send button, the attach clip and the tray of attachments above the text. [isEnabled] is false in
// guided mode and while a restriction holds. [showsAttach] is false while the service keeps attachments off, and
// [isAttachEnabled] is false as well while a turn runs, a send waits, or the message has its four attachments.
// [isSendEnabled] needs text or an attachment, and every attachment kept by the service. While a turn can be stopped,
// [isStoppable] puts the stop button where the send button is. [textRevision] moves when the chat replaces the text
// (the send clears it, a refused send gives it back), so the field adopts it instead of taking it for an echo.
data class ChatComposerItem(
	val text: String,
	val isEnabled: Boolean,
	val isSendEnabled: Boolean,
	val showsAttach: Boolean = false,
	val isAttachEnabled: Boolean = false,
	val attachments: List<ChatAttachmentItem> = emptyList(),
	val isStoppable: Boolean = false,
	val textRevision: Int = 0
)
