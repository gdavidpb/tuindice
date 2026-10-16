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
			recordDataPrerequisiteStateOf(isReady = syncState?.hasSynced == true, syncStatus = syncStatus)
		}.distinctUntilChanged()
	}
}

// What the sync says of the record data. Without the record stored, a blocking status fails it for every reader of it,
// and a new student the university has no record for yet is also reported as a state of its own, which Summary reads
// as the first term to come and not as an error.
internal fun recordDataPrerequisiteStateOf(isReady: Boolean, syncStatus: SyncStatus): RecordDataPrerequisiteState {
	return RecordDataPrerequisiteState(
		isReady = isReady,
		hasFailed = !isReady && syncStatus.isBlockingFailure(),
		isNewStudentWithoutRecord = !isReady && syncStatus == SyncStatus.NewStudentNoRecord
	)
}

private fun SyncStatus.isBlockingFailure(): Boolean {
	return when (this) {
		SyncStatus.Failed,
		SyncStatus.Unavailable,
		SyncStatus.OutdatedCredentials,
		SyncStatus.MissingCredentials,
		SyncStatus.NewStudentNoRecord,
		SyncStatus.RecordAccessDenied,
		-> true

		SyncStatus.Healthy -> false
	}
}
