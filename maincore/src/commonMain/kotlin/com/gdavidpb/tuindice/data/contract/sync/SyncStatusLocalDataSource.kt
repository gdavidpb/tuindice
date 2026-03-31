package com.gdavidpb.tuindice.data.contract.sync


import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface SyncStatusLocalDataSource {
	fun observeSyncStatus(): Flow<SyncStatus>
	suspend fun getSyncStatus(): SyncStatus
	suspend fun setSyncStatus(status: SyncStatus)
}
