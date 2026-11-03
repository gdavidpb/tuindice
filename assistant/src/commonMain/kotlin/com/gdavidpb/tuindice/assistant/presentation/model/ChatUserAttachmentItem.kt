package com.gdavidpb.tuindice.assistant.presentation.model

// One photo of a message the student sent, as its bubble shows it: its [thumbnail], and the [image] it was sent as
// while the conversation keeps it (an older photo opens its thumbnail large).
data class ChatUserAttachmentItem(
	val name: String,
	val thumbnail: ByteArray?,
	val image: ByteArray?
)
