package com.gdavidpb.tuindice.record.domain.repository

import com.gdavidpb.tuindice.record.domain.model.RecordViewMode

interface QuarterSelectionRepository {
	suspend fun getSelectedQuarterId(viewMode: RecordViewMode): String?
	suspend fun setSelectedQuarterId(viewMode: RecordViewMode, quarterId: String)
	suspend fun getRecordViewMode(): RecordViewMode
	suspend fun setRecordViewMode(viewMode: RecordViewMode)
}
