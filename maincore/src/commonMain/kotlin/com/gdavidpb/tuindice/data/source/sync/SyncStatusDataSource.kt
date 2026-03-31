package com.gdavidpb.tuindice.data.source.sync

import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.data.contract.sync.SyncStatusLocalDataSource
import kotlinx.coroutines.flow.Flow

class SyncStatusDataSource(
	private val localDataSource: SyncStatusLocalDataSource
) : SyncStatusRepository {
	override fun observeSyncStatus(): Flow<SyncStatus> {
		return localDataSource.observeSyncStatus()
	}

	override suspend fun getSyncStatus(): SyncStatus {
		return localDataSource.getSyncStatus()
	}

	override suspend fun setSyncStatus(status: SyncStatus) {
		localDataSource.setSyncStatus(status)
	}
}
