package com.gdavidpb.tuindice.record.data.repository

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

interface AcademicRecordLocalDataRepository {
	fun observeAcademicRecordFlow(): Flow<AcademicRecord?>
	fun observeHasSyncedRecordFlow(): Flow<Boolean>
	fun observeAcademicRecordSnapshotFlow(): Flow<ObservedSyncedSnapshot<AcademicRecord?>> {
		return combine(
			observeAcademicRecordFlow(),
			observeHasSyncedRecordFlow()
		) { record, hasSyncedRecord ->
			ObservedSyncedSnapshot(
				value = record,
				hasSynced = hasSyncedRecord
			)
		}
	}
	suspend fun hasAcademicRecord(): Boolean
	suspend fun getAcademicRecord(): AcademicRecord?
	suspend fun getRecordRevision(): Long?
	suspend fun saveAcademicRecord(record: VersionedAcademicRecord)

	/**
	 * Stores [record] unless the stored one is newer. The revision is the server's order for the record: a lower one
	 * is an older copy of it and is not written. The comparison and the write happen in one transaction, so a write
	 * that lands in between cannot be overwritten by an older record. Returns whether [record] was written.
	 */
	suspend fun replaceAcademicRecordIfNotOlder(record: VersionedAcademicRecord): Boolean
}
