package com.gdavidpb.tuindice.summary.domain.repository

import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import kotlinx.coroutines.flow.Flow

/**
 * The backgrounds a term card can wear, the app's own first, as they were last saved from the catalog. Reading them is
 * local and never waits for the server: [refreshShareBackgrounds] asks the server for the catalog and saves it. A
 * refresh that fails keeps the list that was saved, and the app's own background is always there.
 */
interface ShareBackgroundRepository {
	fun observeShareBackgrounds(): Flow<List<ShareBackground>>

	suspend fun refreshShareBackgrounds()
}
