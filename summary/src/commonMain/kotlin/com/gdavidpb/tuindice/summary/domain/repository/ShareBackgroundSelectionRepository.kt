package com.gdavidpb.tuindice.summary.domain.repository

import kotlinx.coroutines.flow.Flow

/** The background the student chose last, observed so that the next card opens with it. */
interface ShareBackgroundSelectionRepository {
	fun observePreferredBackgroundId(): Flow<String?>

	suspend fun setPreferredBackgroundId(id: String)
}
