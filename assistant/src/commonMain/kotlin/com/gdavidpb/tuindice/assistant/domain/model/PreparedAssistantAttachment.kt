package com.gdavidpb.tuindice.assistant.domain.model

/**
 * A file the student picked, read on the device and ready to send: its [kind], its [name], the [content] the upload
 * sends and, for a photo, the [thumbnail] the message shows.
 */
data class PreparedAssistantAttachment(
	val kind: AssistantAttachmentKind,
	val name: String,
	val content: AssistantAttachmentContent,
	val thumbnail: ByteArray?
)
