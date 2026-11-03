package com.gdavidpb.tuindice.assistant.domain.model

/**
 * A photo as the student sent it, shown in the message: its [name], its [thumbnail], and the [image] as it was sent.
 * The image is kept in memory for the newest photos only (see the conversation's data source), so the message can open
 * it large; an older photo opens its thumbnail.
 */
data class AssistantUserAttachment(
	val name: String,
	val thumbnail: ByteArray?,
	val image: ByteArray?
)
