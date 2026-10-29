package com.gdavidpb.tuindice.chatcontract.extraction

/** What the app sends to an extraction, and what the service accepts. */
public object ExtractionLimits {
	/** The longest edge of the image, in pixels, after the app scales it down. */
	public const val MAX_IMAGE_EDGE_PX: Int = 1568

	/** The size of the encoded image the service accepts, in bytes of base64 text. */
	public const val MAX_IMAGE_BASE64_BYTES: Int = 5_000_000

	/** The media types an image can have. */
	public val MEDIA_TYPES: List<String> = listOf("image/jpeg", "image/png", "image/webp")
}
