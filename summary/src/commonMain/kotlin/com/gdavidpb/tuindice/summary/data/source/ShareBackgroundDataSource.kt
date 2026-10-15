package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.summary.data.mapper.toShareBackground
import com.gdavidpb.tuindice.summary.data.repository.share.ShareBackgroundRemoteDataRepository
import com.gdavidpb.tuindice.summary.data.repository.share.ShareBackgroundSettingsDataRepository
import com.gdavidpb.tuindice.summary.domain.mapper.shareBackgroundsOf
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.domain.repository.ShareBackgroundRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ShareBackgroundDataSource(
	private val remoteDataSource: ShareBackgroundRemoteDataRepository,
	private val settingsDataSource: ShareBackgroundSettingsDataRepository
) : ShareBackgroundRepository {
	override fun observeShareBackgrounds(): Flow<List<ShareBackground>> {
		return settingsDataSource.observeLastCatalog().map { catalog ->
			val backgrounds = catalog.orEmpty()
				.sortedBy { background -> background.order }
				.map { background -> background.toShareBackground() }

			shareBackgroundsOf(backgrounds)
		}
	}

	// The catalog as the server gives it now. A catalog that cannot be read keeps the one that was saved: it is no reason
	// to fail the picker, and cancellation is not a failure.
	override suspend fun refreshShareBackgrounds() {
		val catalog = runCatching { remoteDataSource.getShareBackgrounds() }
			.onFailure { failure -> if (failure is CancellationException) throw failure }
			.getOrNull()
			?: return

		settingsDataSource.saveLastCatalog(catalog)
	}
}
