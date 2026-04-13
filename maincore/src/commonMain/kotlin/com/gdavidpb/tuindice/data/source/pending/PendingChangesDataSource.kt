package com.gdavidpb.tuindice.data.source.pending

import com.gdavidpb.tuindice.base.domain.model.FlushPendingChangesResult
import com.gdavidpb.tuindice.base.domain.model.PendingChanges
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.repository.PendingChangesRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.evaluations.data.mutation.EVALUATIONS_MUTATION_SCOPE
import com.gdavidpb.tuindice.evaluations.data.mutation.EVALUATIONS_MUTATION_STORE_ID
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.persistence.data.room.daos.PendingMutationDao
import com.gdavidpb.tuindice.record.data.mutation.RECORD_MUTATION_SCOPE
import com.gdavidpb.tuindice.record.data.mutation.RECORD_MUTATION_STORE_ID
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository

class PendingChangesDataSource(
	private val pendingMutationDao: PendingMutationDao,
	private val academicRecordRepository: AcademicRecordRepository,
	private val evaluationRepository: EvaluationRepository,
	private val syncStatusRepository: SyncStatusRepository
) : PendingChangesRepository {
	override suspend fun getPendingChanges(): PendingChanges {
		val recordMutations = pendingMutationDao.getMutations(
			storeId = RECORD_MUTATION_STORE_ID,
			scopeKey = RECORD_MUTATION_SCOPE
		)
		val evaluationMutations = pendingMutationDao.getMutations(
			storeId = EVALUATIONS_MUTATION_STORE_ID,
			scopeKey = EVALUATIONS_MUTATION_SCOPE
		)
		val allMutations = recordMutations + evaluationMutations

		return PendingChanges(
			totalCount = allMutations.size,
			recordCount = recordMutations.size,
			evaluationsCount = evaluationMutations.size,
			hasFailedMutations = allMutations.any { mutation -> mutation.status == "Failed" }
		)
	}

	override suspend fun flushPendingChanges(): FlushPendingChangesResult {
		val initialPendingChanges = getPendingChanges()
		if (initialPendingChanges.totalCount == 0) {
			return FlushPendingChangesResult.Success
		}

		if (syncStatusRepository.getSyncStatus() == SyncStatus.OutdatedCredentials) {
			return FlushPendingChangesResult.OutdatedCredentials(initialPendingChanges)
		}

		retryFailedMutations(
			storeId = RECORD_MUTATION_STORE_ID,
			scopeKey = RECORD_MUTATION_SCOPE
		)
		academicRecordRepository.drainPendingMutations()

		val recordPendingChanges = getPendingChanges()
		if (syncStatusRepository.getSyncStatus() == SyncStatus.OutdatedCredentials) {
			return FlushPendingChangesResult.OutdatedCredentials(recordPendingChanges)
		}

		retryFailedMutations(
			storeId = EVALUATIONS_MUTATION_STORE_ID,
			scopeKey = EVALUATIONS_MUTATION_SCOPE
		)
		evaluationRepository.drainPendingMutations()

		val finalPendingChanges = getPendingChanges()
		if (syncStatusRepository.getSyncStatus() == SyncStatus.OutdatedCredentials) {
			return FlushPendingChangesResult.OutdatedCredentials(finalPendingChanges)
		}

		return if (finalPendingChanges.totalCount == 0) {
			FlushPendingChangesResult.Success
		} else {
			FlushPendingChangesResult.PendingRemaining(finalPendingChanges)
		}
	}

	private suspend fun retryFailedMutations(
		storeId: String,
		scopeKey: String
	) {
		pendingMutationDao.retryFailedMutations(
			storeId = storeId,
			scopeKey = scopeKey,
			updatedAt = currentTimeMillis()
		)
	}
}
