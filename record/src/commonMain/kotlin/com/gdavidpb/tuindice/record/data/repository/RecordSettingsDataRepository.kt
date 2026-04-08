package com.gdavidpb.tuindice.record.data.repository

import com.gdavidpb.tuindice.record.domain.model.RecordViewMode

interface RecordSettingsDataRepository {
	suspend fun isGetAcademicRecordOnCooldown(): Boolean
	suspend fun setGetAcademicRecordOnCooldown()
	fun getSelectedTermId(viewMode: RecordViewMode): String?
	fun setSelectedTermId(viewMode: RecordViewMode, termId: String)
	fun getRecordViewMode(): RecordViewMode
	fun setRecordViewMode(viewMode: RecordViewMode)
}
