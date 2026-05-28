package com.gdavidpb.tuindice.data.source.prerequisite

import com.gdavidpb.tuindice.base.domain.model.RecordDataPrerequisiteState
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.repository.RecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicRecordSyncStateDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

class RecordDataPrerequisiteDataSource(
	private val academicRecordSyncStateDao: AcademicRecordSyncStateDao,
	private val syncStatusRepository: SyncStatusRepository
) : RecordDataPrerequisiteRepository {
	override fun observeRecordDataPrerequisiteFlow(): Flow<RecordDataPrerequisiteState> {
		return combine(
			academicRecordSyncStateDao.observeSyncState(),
			syncStatusRepository.observeSyncStatus()
		) { syncState, syncStatus ->
			val isReady = syncState?.hasSynced == true
			RecordDataPrerequisiteState(
				isReady = isReady,
				hasFailed = !isReady && syncStatus.isBlockingFailure()
			)
		}.distinctUntilChanged()
	}

	override suspend fun isRecordDataReady(): Boolean {
		return academicRecordSyncStateDao.getSyncState()?.hasSynced == true
	}

	private fun SyncStatus.isBlockingFailure(): Boolean {
		return when (this) {
			SyncStatus.Failed,
			SyncStatus.Unavailable,
			SyncStatus.OutdatedCredentials,
			-> true

			SyncStatus.Healthy -> false
		}
	}
}
