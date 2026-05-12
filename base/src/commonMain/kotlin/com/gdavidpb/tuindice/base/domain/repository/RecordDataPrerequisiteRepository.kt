package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.RecordDataPrerequisiteState
import kotlinx.coroutines.flow.Flow

interface RecordDataPrerequisiteRepository {
	fun observeRecordDataPrerequisiteFlow(): Flow<RecordDataPrerequisiteState>
	suspend fun isRecordDataReady(): Boolean
}
