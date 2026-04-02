package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.record.data.repository.QuarterSettingsDataRepository
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.repository.QuarterSelectionRepository

class QuarterSelectionDataSource(
	private val settingsDataSource: QuarterSettingsDataRepository
) : QuarterSelectionRepository {
	override suspend fun getSelectedQuarterId(viewMode: RecordViewMode): String? {
		return settingsDataSource.getSelectedQuarterId(viewMode = viewMode)
	}

	override suspend fun setSelectedQuarterId(viewMode: RecordViewMode, quarterId: String) {
		settingsDataSource.setSelectedQuarterId(
			viewMode = viewMode,
			quarterId = quarterId
		)
	}

	override suspend fun getRecordViewMode(): RecordViewMode {
		return settingsDataSource.getRecordViewMode()
	}

	override suspend fun setRecordViewMode(viewMode: RecordViewMode) {
		settingsDataSource.setRecordViewMode(viewMode)
	}
}
