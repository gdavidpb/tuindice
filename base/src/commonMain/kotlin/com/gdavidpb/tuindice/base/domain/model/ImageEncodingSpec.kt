package com.gdavidpb.tuindice.base.domain.model

/**
 * Limits applied when an image is reduced before it is sent.
 *
 * @property maxDimensionPx largest allowed side of the encoded image, in pixels; must be positive
 * @property maxBytes largest allowed size of the encoded image, in bytes; must be positive
 * @property jpegQuality JPEG quality, from 1 to 100
 */
data class ImageEncodingSpec(
	val maxDimensionPx: Int,
	val maxBytes: Int,
	val jpegQuality: Int
) {
	init {
		require(maxDimensionPx > 0) { "Max dimension must be positive." }
		require(maxBytes > 0) { "Max bytes must be positive." }
		require(jpegQuality in MIN_JPEG_QUALITY..MAX_JPEG_QUALITY) {
			"JPEG quality must be between $MIN_JPEG_QUALITY and $MAX_JPEG_QUALITY."
		}
	}

	private companion object {
		const val MIN_JPEG_QUALITY = 1
		const val MAX_JPEG_QUALITY = 100
	}
}
