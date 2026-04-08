package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.record.data.repository.RecordSettingsDataRepository
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.repository.RecordSelectionRepository

class RecordSelectionDataSource(
	private val settingsDataSource: RecordSettingsDataRepository
) : RecordSelectionRepository {
	override suspend fun getSelectedTermId(viewMode: RecordViewMode): String? {
		return settingsDataSource.getSelectedTermId(viewMode)
	}

	override suspend fun setSelectedTermId(viewMode: RecordViewMode, termId: String) {
		settingsDataSource.setSelectedTermId(viewMode, termId)
	}

	override suspend fun getRecordViewMode(): RecordViewMode {
		return settingsDataSource.getRecordViewMode()
	}

	override suspend fun setRecordViewMode(viewMode: RecordViewMode) {
		settingsDataSource.setRecordViewMode(viewMode)
	}
}
