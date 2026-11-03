package com.gdavidpb.tuindice.base.domain.model

import com.gdavidpb.tuindice.base.domain.exception.EncodedImageTooLargeException

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

	/**
	 * Refuses an encoded image larger than [maxBytes]: the encoders call it with the size of the bytes they return,
	 * so the limit is one rule, reported as [EncodedImageTooLargeException].
	 */
	fun checkEncodedSize(encodedBytes: Int) {
		if (encodedBytes > maxBytes) {
			throw EncodedImageTooLargeException(encodedBytes = encodedBytes, maxBytes = maxBytes)
		}
	}

	private companion object {
		const val MIN_JPEG_QUALITY = 1
		const val MAX_JPEG_QUALITY = 100
	}
}
