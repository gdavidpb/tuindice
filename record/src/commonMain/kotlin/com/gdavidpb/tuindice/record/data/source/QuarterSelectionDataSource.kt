package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.record.data.repository.QuarterSettingsDataRepository
import com.gdavidpb.tuindice.record.domain.repository.QuarterSelectionRepository

class QuarterSelectionDataSource(
	private val settingsDataSource: QuarterSettingsDataRepository
) : QuarterSelectionRepository {
	override suspend fun getSelectedQuarterId(): String? {
		return settingsDataSource.getSelectedQuarterId()
	}

	override suspend fun setSelectedQuarterId(quarterId: String) {
		settingsDataSource.setSelectedQuarterId(quarterId)
	}
}
