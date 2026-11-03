package com.gdavidpb.tuindice.assistant.domain.model

/**
 * A photo of the message being written, under the draft's [localId]. [name] comes with the file. [thumbnail] is set
 * once the photo is read. [content] holds the bytes to send until the message leaves.
 */
data class AssistantAttachment(
	val localId: String,
	val name: String,
	val thumbnail: ByteArray?,
	val state: AssistantAttachmentState,
	val content: AssistantAttachmentContent?
)
