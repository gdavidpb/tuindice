package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentKind

// One attachment of a message the student sent, as its bubble shows it: a photo with its thumbnail and the image it was
// sent as, or a PDF with its name and its pages.
data class ChatUserAttachmentItem(
	val kind: AssistantAttachmentKind,
	val name: String,
	val detail: String?,
	val thumbnail: ByteArray?,
	val image: ByteArray?
)
