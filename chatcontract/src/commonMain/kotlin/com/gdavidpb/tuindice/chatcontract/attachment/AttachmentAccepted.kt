package com.gdavidpb.tuindice.chatcontract.attachment

import kotlinx.serialization.Serializable

/**
 * Answer of the service once it has kept an attachment. [name] is the name it kept (cut, or its own when the upload
 * had none); [pageCount] is set for a PDF.
 */
@Serializable
public data class AttachmentAccepted(
	public val attachmentId: String,
	public val mediaType: String,
	public val sizeBytes: Long,
	public val pageCount: Int? = null,
	public val expiresAtMillis: Long,
	public val name: String? = null
)
