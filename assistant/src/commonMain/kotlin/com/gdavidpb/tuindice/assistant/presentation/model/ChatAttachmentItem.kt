package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentKind

// One attachment of the tray above the text: its [name], its [detail] (the pages of a PDF, once the service reads
// them), the [thumbnail] of a photo, and where it stands. A failed one shows its [failureText]; only one that
// [canRetry] offers the retry, the others are taken out.
data class ChatAttachmentItem(
	val localId: String,
	val kind: AssistantAttachmentKind,
	val name: String,
	val detail: String?,
	val thumbnail: ByteArray?,
	val isUploading: Boolean,
	val isFailed: Boolean,
	val canRetry: Boolean,
	val failureText: String?
)
