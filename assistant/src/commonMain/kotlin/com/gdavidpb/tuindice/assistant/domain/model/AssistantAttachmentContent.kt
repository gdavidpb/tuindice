package com.gdavidpb.tuindice.assistant.domain.model

/**
 * The bytes of a photo as the upload sends them, already encoded. [sha256] is their SHA-256 in lowercase hex, the id
 * the upload carries. They stay in memory until the message leaves, so a retry sends them again.
 */
class AssistantAttachmentContent(
	val mediaType: String,
	val sha256: String,
	val bytes: ByteArray
)
