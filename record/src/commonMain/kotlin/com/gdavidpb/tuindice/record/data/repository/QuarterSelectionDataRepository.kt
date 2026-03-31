package com.gdavidpb.tuindice.record.data.repository

import com.gdavidpb.tuindice.record.data.source.QuarterSettingsDataSource
import com.gdavidpb.tuindice.record.domain.repository.QuarterSelectionRepository

class QuarterSelectionDataRepository(
	private val settingsDataSource: QuarterSettingsDataSource
) : QuarterSelectionRepository {
	override suspend fun getSelectedQuarterId(): String? {
		return settingsDataSource.getSelectedQuarterId()
	}

	override suspend fun setSelectedQuarterId(quarterId: String) {
		settingsDataSource.setSelectedQuarterId(quarterId)
	}
}
