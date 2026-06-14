package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface SyncStatusRepository {
	fun observeSyncStatus(): Flow<SyncStatus>
	fun observeLastSuccessfulSyncAt(): Flow<Long?>
	suspend fun getSyncStatus(): SyncStatus
	suspend fun getLastSuccessfulSyncAt(): Long?
	suspend fun setSyncStatus(status: SyncStatus)
	suspend fun setLastSuccessfulSyncAt(timestamp: Long)
	suspend fun reset()
}
