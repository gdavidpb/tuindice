package com.gdavidpb.tuindice.summary.data.repository.share

import com.gdavidpb.tuindice.summary.data.model.ShareBackgroundResponse
import kotlinx.coroutines.flow.Flow

interface ShareBackgroundSettingsDataRepository {
	// The catalog as it was saved the last time it could be read, or null when it never was.
	fun observeLastCatalog(): Flow<List<ShareBackgroundResponse>?>

	suspend fun saveLastCatalog(backgrounds: List<ShareBackgroundResponse>)
}
