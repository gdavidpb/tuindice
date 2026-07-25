package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicAttemptDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicAttemptOverrideDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicRecordDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicRecordSyncStateDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicTermDao
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicRecordEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicRecordSyncStateEntity
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceTransactionRunner
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordLocalDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AcademicRecordRoomDataSource(
	private val academicRecordDao: AcademicRecordDao,
	private val academicRecordSyncStateDao: AcademicRecordSyncStateDao,
	private val academicTermDao: AcademicTermDao,
	private val academicAttemptDao: AcademicAttemptDao,
	private val academicAttemptOverrideDao: AcademicAttemptOverrideDao,
	private val transactionRunner: PersistenceTransactionRunner
) : AcademicRecordLocalDataRepository {
	private val writeMutex = Mutex()

	override fun observeAcademicRecordFlow(): Flow<AcademicRecord?> {
		return observeAcademicRecordSnapshotFlow()
			.map { snapshot -> snapshot.value }
	}

	override fun observeAcademicRecordSnapshotFlow(): Flow<ObservedSyncedSnapshot<AcademicRecord?>> {
		return combine(
			academicRecordDao.observeRecordFlow(),
			academicTermDao.observeTermsFlow(),
			academicAttemptDao.observeAttemptsFlow(),
			academicAttemptOverrideDao.observeOverridesFlow(),
			academicRecordSyncStateDao.observeSyncState()
		) { recordEntity, terms, attempts, overrides, syncState ->
			ObservedSyncedSnapshot(
				value = recordEntity?.let { persistedRecord ->
					AcademicRecord(
						id = persistedRecord.id,
						terms = terms.toAcademicTerms(attempts),
						attemptOverrides = overrides.map { override -> override.toAttemptOverride() }
					)
				},
				hasSynced = syncState?.hasSynced == true
			)
		}
	}

	override fun observeHasSyncedRecordFlow(): Flow<Boolean> {
		return academicRecordSyncStateDao.observeSyncState()
			.map { syncState -> syncState?.hasSynced == true }
	}

	override suspend fun getAcademicRecord(): AcademicRecord? {
		return observeAcademicRecordFlow().first()
	}

	override suspend fun hasAcademicRecord(): Boolean {
		return academicRecordDao.getRecord() != null
	}

	override suspend fun getRecordRevision(): Long? {
		return academicRecordDao.getRecord()?.revisionValue()
	}

	override suspend fun saveAcademicRecord(record: VersionedAcademicRecord) {
		writeMutex.withLock {
			persistVersionedRecord(record)
		}
	}

	private suspend fun persistVersionedRecord(record: VersionedAcademicRecord) {
		transactionRunner.immediate {
			academicAttemptOverrideDao.deleteAll()
			academicAttemptDao.deleteAll()
			academicTermDao.deleteAll()
			academicRecordDao.deleteAll()
			academicRecordDao.upsertEntity(
				AcademicRecordEntity(
					id = record.record.id,
					revision = record.revision,
					updatedAt = currentTimeMillis()
				)
			)
			academicRecordSyncStateDao.upsertEntity(
				AcademicRecordSyncStateEntity(hasSynced = true)
			)
			academicTermDao.upsertEntities(record.record.terms.map { term ->
				term.toAcademicTermEntity()
			})
			academicAttemptDao.upsertEntities(record.record.terms.flatMap { term ->
				term.attempts.mapIndexed { index, attempt ->
					attempt.toAcademicAttemptEntity(
						termId = term.id,
						positionInTerm = index
					)
				}
			})
			if (record.record.attemptOverrides.isNotEmpty()) {
				academicAttemptOverrideDao.upsertEntities(record.record.attemptOverrides.map { override ->
					override.toAcademicAttemptOverrideEntity()
				})
			}
		}
	}
}
