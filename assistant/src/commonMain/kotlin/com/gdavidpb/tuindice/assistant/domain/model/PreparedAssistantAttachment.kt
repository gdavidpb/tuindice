package com.gdavidpb.tuindice.assistant.domain.model

/**
 * A photo the student picked, read on the device and ready to send: its [name], the [content] the upload sends, and the
 * [thumbnail] the message shows.
 */
data class PreparedAssistantAttachment(
	val name: String,
	val content: AssistantAttachmentContent,
	val thumbnail: ByteArray?
)
