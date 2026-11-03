package com.gdavidpb.tuindice.assistant.presentation.model

// One photo of the tray above the text: its [name], its [thumbnail], and where it stands. A failed one shows its
// [failureText]; only one that [canRetry] offers the retry, the others are taken out.
data class ChatAttachmentItem(
	val localId: String,
	val name: String,
	val thumbnail: ByteArray?,
	val isUploading: Boolean,
	val isFailed: Boolean,
	val canRetry: Boolean,
	val failureText: String?
)
