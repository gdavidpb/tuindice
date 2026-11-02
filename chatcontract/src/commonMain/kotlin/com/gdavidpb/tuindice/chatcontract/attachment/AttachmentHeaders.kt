package com.gdavidpb.tuindice.chatcontract.attachment

/**
 * Headers of an upload to `POST /assistant/v1/attachments`. The body is the file's bytes and Content-Type its media
 * type, one of [AttachmentLimits.MEDIA_TYPES].
 */
public object AttachmentHeaders {
	/**
	 * Required. The SHA-256 of the body, as 64 lowercase hex characters. The service scopes it to the student, so a
	 * repeated upload of the same bytes, under any name, gives back the first attachment.
	 */
	public const val CLIENT_ATTACHMENT_ID: String = "X-Client-Attachment-Id"

	/** Optional. The file's name, written with [AttachmentName.encode]; missing or unreadable, the service names it. */
	public const val NAME: String = "X-Attachment-Name"
}
