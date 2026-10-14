package com.gdavidpb.tuindice.summary.domain.repository

import com.gdavidpb.tuindice.summary.domain.model.ShareBackground

/**
 * The backgrounds a term card can wear, the app's own first, and the one the student chose last. A catalog that cannot
 * be read is not an error here: the last list that was read is served, and the app's own background is always there.
 */
interface ShareBackgroundRepository {
	suspend fun getShareBackgrounds(): List<ShareBackground>

	suspend fun getPreferredBackgroundId(): String?

	suspend fun setPreferredBackgroundId(id: String)
}
