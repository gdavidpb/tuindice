package com.gdavidpb.tuindice.data.source.pending

import com.gdavidpb.tuindice.base.domain.model.FlushPendingChangesResult
import com.gdavidpb.tuindice.base.domain.model.PendingChanges
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.base.domain.repository.PendingChangesRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.evaluations.data.mutation.EVALUATIONS_MUTATION_SCOPE
import com.gdavidpb.tuindice.evaluations.data.mutation.EVALUATIONS_MUTATION_STORE_ID
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.persistence.data.room.daos.PendingMutationDao
import com.gdavidpb.tuindice.persistence.domain.record.RECORD_MUTATION_SCOPE
import com.gdavidpb.tuindice.persistence.domain.record.RECORD_MUTATION_STORE_ID
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import kotlinx.coroutines.flow.firstOrNull

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
			hasFailedMutations = allMutations.any { mutation ->
				mutation.status == PendingMutationStatus.Failed.name ||
					mutation.status == PendingMutationStatus.FailedTerminal.name
			}
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

		discardTerminallyRejectedRecordMutations()
		requeueRetryableMutations(
			storeId = RECORD_MUTATION_STORE_ID,
			scopeKey = RECORD_MUTATION_SCOPE
		)
		academicRecordRepository.drainPendingMutations()

		val recordPendingChanges = getPendingChanges()
		if (syncStatusRepository.getSyncStatus() == SyncStatus.OutdatedCredentials) {
			return FlushPendingChangesResult.OutdatedCredentials(recordPendingChanges)
		}

		requeueRetryableMutations(
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

	// Only Failed rows are retryable. Requeuing FailedTerminal ones re-sent mutations the
	// server had already refused for good: each flush spent a doomed request and parked the
	// row again, so the dialog reported pending changes that could never drain.
	private suspend fun requeueRetryableMutations(
		storeId: String,
		scopeKey: String
	) {
		pendingMutationDao.requeueMutations(
			storeId = storeId,
			scopeKey = scopeKey,
			requeueFrom = PendingMutationStatus.Failed.name,
			retryableBefore = Long.MAX_VALUE,
			updatedAt = currentTimeMillis()
		)
	}

	// A terminal rejection reaching the flush means the user never passed through Record to
	// see it acknowledged. Sign-out is the last chance to reconcile: discard those envelopes
	// so the count reflects only work that can still land. Evaluations has no acknowledge
	// surface yet, so its terminal rows simply stop being requeued above.
	private suspend fun discardTerminallyRejectedRecordMutations() {
		val rejectedMutationIds = academicRecordRepository
			.observeTerminallyRejectedMutationIdsFlow()
			.firstOrNull()
			.orEmpty()

		if (rejectedMutationIds.isEmpty()) return

		academicRecordRepository.acknowledgeTerminallyRejectedMutations(rejectedMutationIds)
	}
}
