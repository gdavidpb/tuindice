package com.gdavidpb.tuindice.assistant.domain.model

/**
 * An attachment of the message being written, under the draft's [localId]. [name] and [kind] come with the file. Its
 * [sizeBytes] are those of the bytes sent, zero until the file is read. [pageCount] comes with the service's answer for
 * a PDF. [thumbnail] is set for a photo once it is read. [content] holds the bytes to send until the message leaves.
 */
data class AssistantAttachment(
	val localId: String,
	val kind: AssistantAttachmentKind,
	val name: String,
	val sizeBytes: Long,
	val pageCount: Int?,
	val thumbnail: ByteArray?,
	val state: AssistantAttachmentState,
	val content: AssistantAttachmentContent?
)
