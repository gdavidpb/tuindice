package com.gdavidpb.tuindice.chatcontract.attachment

/** What a message can carry, and what the service accepts when an attachment (a photo) is uploaded. */
public object AttachmentLimits {
	/** How many attachments one message can carry. */
	public const val MAX_PER_MESSAGE: Int = 4

	/** The longest edge of a photo, in pixels, after the app scales it down. */
	public const val MAX_IMAGE_EDGE_PX: Int = 1568

	/** The largest size of an encoded photo, in bytes. */
	public const val MAX_IMAGE_BYTES: Int = 3_500_000

	/**
	 * The largest size of the photos one request to the model carries, in bytes: the message's and the ones the service
	 * sends again from earlier messages, so the request stays within the provider's limit.
	 */
	public const val MAX_BYTES_PER_MESSAGE: Int = 16_777_216

	/** How many attachments a student can upload in a day (Caracas time); past it the answer carries a retry time. */
	public const val MAX_UPLOADS_PER_DAY: Int = 40

	/** The longest file name the service keeps, in Unicode code points; a longer one is cut. */
	public const val MAX_NAME_LENGTH: Int = 120

	/** How many days the service keeps an attachment; after that only its name is left. */
	public const val RETENTION_DAYS: Int = 30

	/** The media types a photo can have. */
	public val IMAGE_MEDIA_TYPES: List<String> = listOf("image/jpeg", "image/png", "image/webp")

	/**
	 * Every media type an attachment can have: an attachment is a photo. The service compares the upload's Content-Type by
	 * type and subtype, ignoring case and parameters.
	 */
	public val MEDIA_TYPES: List<String> = IMAGE_MEDIA_TYPES
}
