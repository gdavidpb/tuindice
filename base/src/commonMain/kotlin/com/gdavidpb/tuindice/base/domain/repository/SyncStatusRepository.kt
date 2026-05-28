package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface SyncStatusRepository {
	fun observeSyncStatus(): Flow<SyncStatus>
	suspend fun getSyncStatus(): SyncStatus
	suspend fun setSyncStatus(status: SyncStatus)
	suspend fun reset()
}
