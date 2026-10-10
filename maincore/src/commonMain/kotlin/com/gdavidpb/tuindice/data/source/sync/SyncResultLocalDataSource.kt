package com.gdavidpb.tuindice.data.source.sync

import com.gdavidpb.tuindice.data.model.SyncResult
import com.gdavidpb.tuindice.data.repository.sync.SyncResultLocalDataRepository
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordLocalDataRepository
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordOutboxDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.LocalDataRepository
import kotlinx.coroutines.CancellationException

// The record first, then the user: the same order the sync always wrote them in, so a failure
// between the two leaves the record (the larger write) in place for the next sync to reconcile.
class SyncResultLocalDataSource(
	private val recordLocalDataSource: AcademicRecordLocalDataRepository,
	private val userLocalDataSource: LocalDataRepository,
	private val recordOutboxDataSource: AcademicRecordOutboxDataRepository
) : SyncResultLocalDataRepository {
	override suspend fun saveSyncResult(result: SyncResult) {
		recordLocalDataSource.saveAcademicRecord(result.record)
		// The record just replaced may no longer have what the queued edits were aimed at.
		runCatching { recordOutboxDataSource.reconcileWithConfirmedRecord(result.record) }
			.onFailure { throwable -> if (throwable is CancellationException) throw throwable }
		userLocalDataSource.updateUser(result.user)
	}
}
