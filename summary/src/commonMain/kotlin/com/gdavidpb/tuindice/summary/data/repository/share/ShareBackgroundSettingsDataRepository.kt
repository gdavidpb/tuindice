package com.gdavidpb.tuindice.summary.data.repository.share

import com.gdavidpb.tuindice.summary.data.model.ShareBackgroundResponse

interface ShareBackgroundSettingsDataRepository {
	// The catalog as it was the last time it could be read, or null when it never was.
	suspend fun getLastCatalog(): List<ShareBackgroundResponse>?

	suspend fun saveLastCatalog(backgrounds: List<ShareBackgroundResponse>)

	suspend fun getPreferredBackgroundId(): String?

	suspend fun setPreferredBackgroundId(id: String)
}
