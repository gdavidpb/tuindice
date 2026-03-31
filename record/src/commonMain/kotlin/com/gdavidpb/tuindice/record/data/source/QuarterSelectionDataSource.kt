package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.record.data.contract.QuarterSettingsDataSource
import com.gdavidpb.tuindice.record.domain.repository.QuarterSelectionRepository

class QuarterSelectionDataSource(
	private val settingsDataSource: QuarterSettingsDataSource
) : QuarterSelectionRepository {
	override suspend fun getSelectedQuarterId(): String? {
		return settingsDataSource.getSelectedQuarterId()
	}

	override suspend fun setSelectedQuarterId(quarterId: String) {
		settingsDataSource.setSelectedQuarterId(quarterId)
	}
}
