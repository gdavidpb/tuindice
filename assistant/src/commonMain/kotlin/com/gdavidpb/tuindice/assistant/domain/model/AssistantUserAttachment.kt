package com.gdavidpb.tuindice.assistant.domain.model

/**
 * An attachment as the student sent it, shown in the message: its [kind], [name] and, for a PDF, its [pageCount]. A
 * photo has its [thumbnail] and the [image] as it was sent, kept in memory so the message can open it large.
 */
data class AssistantUserAttachment(
	val kind: AssistantAttachmentKind,
	val name: String,
	val pageCount: Int?,
	val thumbnail: ByteArray?,
	val image: ByteArray?
)
