package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOverride
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.isSynthetic
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
import com.gdavidpb.tuindice.record.data.mutation.AcademicRecordMutation
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
		return combine(
			academicRecordDao.observeRecordFlow(),
			academicTermDao.observeTermsFlow(),
			academicAttemptDao.observeAttemptsFlow(),
			academicAttemptOverrideDao.observeOverridesFlow()
		) { recordEntity, terms, attempts, overrides ->
			val persistedRecord = recordEntity ?: return@combine null

			AcademicRecord(
				id = persistedRecord.id,
				terms = terms.toAcademicTerms(attempts),
				attemptOverrides = overrides.map { override -> override.toAttemptOverride() }
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

	override suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?,
		committed: Boolean
	): AcademicRecord? {
		return writeMutex.withLock {
			val current = getAcademicRecord() ?: return@withLock null
			val updated = current.copy(
				attemptOverrides = current.attemptOverrides
					.filterNot { override -> override.attemptId == attemptId } +
					AttemptOverride(
						attemptId = attemptId,
						score = score,
						outcome = outcome,
						updatedAtMillis = currentTimeMillis()
					)
			)
			persistVersionedRecord(
				VersionedAcademicRecord(
					revision = getRecordRevision() ?: 0L,
					record = updated
				)
			)
			updated
		}
	}

	override suspend fun deleteAttemptOverride(attemptId: String): AcademicRecord? {
		return writeMutex.withLock {
			val current = getAcademicRecord() ?: return@withLock null
			val updated = current.copy(
				attemptOverrides = current.attemptOverrides.filterNot { override ->
					override.attemptId == attemptId
				}
			)
			persistVersionedRecord(
				VersionedAcademicRecord(
					revision = getRecordRevision() ?: 0L,
					record = updated
				)
			)
			updated
		}
	}

	override suspend fun addSyntheticTerm(command: AcademicRecordMutation.AddSyntheticTerm): AcademicRecord? {
		return writeMutex.withLock {
			val current = getAcademicRecord() ?: return@withLock null
			val updated = current.copy(
				terms = normalizeTerms(
					current.terms.filterNot { term -> term.id == command.termId } + command.toAcademicTerm()
				)
			)
			persistVersionedRecord(
				VersionedAcademicRecord(
					revision = getRecordRevision() ?: 0L,
					record = updated
				)
			)
			updated
		}
	}

	override suspend fun updateSyntheticTerm(command: AcademicRecordMutation.UpdateSyntheticTerm): AcademicRecord? {
		return writeMutex.withLock {
			val current = getAcademicRecord() ?: return@withLock null
			val targetTerm = current.terms.firstOrNull { term ->
				(term.id == command.targetTermId || term.termKey == command.targetTermKey) && term.kind.isSynthetic
			} ?: return@withLock current
			val updatedTerms = normalizeTerms(
				current.terms.filterNot { term ->
					term.id == targetTerm.id || term.termKey == targetTerm.termKey
				} + command.toAcademicTerm()
			)
			val availableAttemptIds = updatedTerms
				.flatMap(AcademicTerm::attempts)
				.map(AcademicAttempt::id)
				.toSet()
			val updated = current.copy(
				terms = updatedTerms,
				attemptOverrides = current.attemptOverrides.filter { override ->
					override.attemptId in availableAttemptIds
				}
			)
			persistVersionedRecord(
				VersionedAcademicRecord(
					revision = getRecordRevision() ?: 0L,
					record = updated
				)
			)
			updated
		}
	}

	override suspend fun deleteSyntheticTerm(termId: String): AcademicRecord? {
		return writeMutex.withLock {
			val current = getAcademicRecord() ?: return@withLock null
			val targetTerm = current.terms.firstOrNull { term ->
				term.id == termId && term.kind.isSynthetic
			} ?: return@withLock current
			val removedAttemptIds = targetTerm.attempts.map(AcademicAttempt::id).toSet()
			val updated = current.copy(
				terms = current.terms.filterNot { term -> term.id == termId },
				attemptOverrides = current.attemptOverrides.filterNot { override ->
					override.attemptId in removedAttemptIds
				}
			)
			persistVersionedRecord(
				VersionedAcademicRecord(
					revision = getRecordRevision() ?: 0L,
					record = updated
				)
			)
			updated
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

	private fun normalizeTerms(terms: List<AcademicTerm>): List<AcademicTerm> {
		return terms.sortedWith(
			compareBy(AcademicTerm::termOrder, AcademicTerm::id)
		)
	}
}
