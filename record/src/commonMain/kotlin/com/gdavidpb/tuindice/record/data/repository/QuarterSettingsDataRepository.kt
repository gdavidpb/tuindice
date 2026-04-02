package com.gdavidpb.tuindice.record.data.repository

import com.gdavidpb.tuindice.record.domain.model.RecordViewMode

interface QuarterSettingsDataRepository {
	suspend fun isGetQuartersOnCooldown(): Boolean
	suspend fun setGetQuartersOnCooldown()
	fun getSelectedQuarterId(viewMode: RecordViewMode): String?
	fun setSelectedQuarterId(viewMode: RecordViewMode, quarterId: String)
	fun getRecordViewMode(): RecordViewMode
	fun setRecordViewMode(viewMode: RecordViewMode)
}
