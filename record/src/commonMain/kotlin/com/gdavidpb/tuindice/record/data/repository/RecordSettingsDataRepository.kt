package com.gdavidpb.tuindice.record.data.repository

import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import kotlinx.coroutines.flow.Flow

interface RecordSettingsDataRepository {
	suspend fun isGetAcademicRecordOnCooldown(): Boolean
	suspend fun setGetAcademicRecordOnCooldown()
	fun observeSelectedTermId(viewMode: RecordViewMode): Flow<String?>
	fun observeRecordViewMode(): Flow<RecordViewMode>
	fun getSelectedTermId(viewMode: RecordViewMode): String?
	fun setSelectedTermId(viewMode: RecordViewMode, termId: String)
	fun getRecordViewMode(): RecordViewMode
	fun setRecordViewMode(viewMode: RecordViewMode)
}
