package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.summary.data.mapper.toShareBackground
import com.gdavidpb.tuindice.summary.data.model.ShareBackgroundResponse
import com.gdavidpb.tuindice.summary.data.repository.share.ShareBackgroundRemoteDataRepository
import com.gdavidpb.tuindice.summary.data.repository.share.ShareBackgroundSettingsDataRepository
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.domain.repository.ShareBackgroundRepository
import kotlinx.coroutines.CancellationException

class ShareBackgroundDataSource(
	private val remoteDataSource: ShareBackgroundRemoteDataRepository,
	private val settingsDataSource: ShareBackgroundSettingsDataRepository
) : ShareBackgroundRepository {
	override suspend fun getShareBackgrounds(): List<ShareBackground> {
		val catalog = readCatalog()
			.sortedBy { background -> background.order }
			.map { background -> background.toShareBackground() }
			.filter { background -> background.id != ShareBackground.INCLUDED_ID }

		return listOf(ShareBackground.included) + catalog
	}

	override suspend fun getPreferredBackgroundId(): String? {
		return settingsDataSource.getPreferredBackgroundId()
	}

	override suspend fun setPreferredBackgroundId(id: String) {
		settingsDataSource.setPreferredBackgroundId(id)
	}

	// The catalog as the server gives it now, or the last list that it gave: a catalog that cannot be read never hides
	// the background the app ships with, and it is no reason to fail the picker. Cancellation is not a failure.
	private suspend fun readCatalog(): List<ShareBackgroundResponse> {
		val backgrounds = runCatching { remoteDataSource.getShareBackgrounds() }
			.onFailure { failure -> if (failure is CancellationException) throw failure }
			.getOrElse { return settingsDataSource.getLastCatalog().orEmpty() }

		settingsDataSource.saveLastCatalog(backgrounds)

		return backgrounds
	}
}
