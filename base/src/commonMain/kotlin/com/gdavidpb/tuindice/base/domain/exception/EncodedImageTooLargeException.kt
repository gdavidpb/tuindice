package com.gdavidpb.tuindice.base.domain.exception

/**
 * An image encoder would return an image larger than the size its spec allows, so it returns none. The image is
 * valid; only its size is over the limit, so this is not an invalid argument.
 *
 * It stays an [IllegalStateException], the type the encoders threw before. Encoding the same picture with the same
 * spec gives the same result, so a retry does not help.
 */
class EncodedImageTooLargeException(
	val encodedBytes: Int,
	val maxBytes: Int
) : IllegalStateException("Image of $encodedBytes bytes exceeds the maximum size of $maxBytes bytes.")
