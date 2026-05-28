package com.gdavidpb.tuindice.record.domain.repository

import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import kotlinx.coroutines.flow.Flow

interface RecordSelectionRepository {
	fun observeSelectedTermId(viewMode: RecordViewMode): Flow<String?>
	fun observeRecordViewMode(): Flow<RecordViewMode>
	suspend fun getSelectedTermId(viewMode: RecordViewMode): String?
	suspend fun setSelectedTermId(viewMode: RecordViewMode, termId: String)
	suspend fun getRecordViewMode(): RecordViewMode
	suspend fun setRecordViewMode(viewMode: RecordViewMode)
}
