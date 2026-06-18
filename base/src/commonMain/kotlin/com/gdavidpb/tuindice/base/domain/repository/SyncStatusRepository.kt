package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.model.SyncReport
import kotlinx.coroutines.flow.Flow

interface SyncStatusRepository {
	fun observeSyncStatus(): Flow<SyncStatus>
	fun observeSyncReport(): Flow<SyncReport>
	fun observeLastSuccessfulSyncAt(): Flow<Long?>
	suspend fun getSyncStatus(): SyncStatus
	suspend fun getSyncReport(): SyncReport
	suspend fun getLastSuccessfulSyncAt(): Long?
	suspend fun setSyncStatus(status: SyncStatus)
	suspend fun setSyncReport(report: SyncReport)
	suspend fun setLastSuccessfulSyncAt(timestamp: Long)
	suspend fun reset()
}
