package com.gdavidpb.tuindice.summary.domain.repository

import com.gdavidpb.tuindice.summary.domain.model.ShareableImage

/**
 * Hands an image to the platform's share sheet. It throws when the image cannot be written or the
 * sheet cannot be shown, so that the caller can report the failure.
 */
interface ImageShareRepository {
	suspend fun shareImage(image: ShareableImage)
}
