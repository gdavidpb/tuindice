package com.gdavidpb.tuindice.data.source.pending

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.FlushPendingChangesResult
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.evaluations.data.mutation.EVALUATIONS_MUTATION_SCOPE
import com.gdavidpb.tuindice.evaluations.data.mutation.EVALUATIONS_MUTATION_STORE_ID
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.persistence.data.room.daos.PendingMutationDao
import com.gdavidpb.tuindice.persistence.data.room.entity.PendingMutationEntity
import com.gdavidpb.tuindice.record.data.mutation.AcademicRecordMutation
import com.gdavidpb.tuindice.record.data.mutation.RECORD_MUTATION_SCOPE
import com.gdavidpb.tuindice.record.data.mutation.RECORD_MUTATION_STORE_ID
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class PendingChangesDataSourceContractTest {
	@Test
	fun getPendingChanges_countsMutationsAcrossScopes() = runTest {
		val pendingMutationDao = FakePendingMutationDao(
			mutations = mutableListOf(
				pendingMutation(
					mutationId = "record-pending",
					storeId = RECORD_MUTATION_STORE_ID,
					scopeKey = RECORD_MUTATION_SCOPE,
					status = PendingMutationStatus.Pending
				),
				pendingMutation(
					mutationId = "record-failed",
					storeId = RECORD_MUTATION_STORE_ID,
					scopeKey = RECORD_MUTATION_SCOPE,
					status = PendingMutationStatus.Failed
				),
				pendingMutation(
					mutationId = "evaluations-pending",
					storeId = EVALUATIONS_MUTATION_STORE_ID,
					scopeKey = EVALUATIONS_MUTATION_SCOPE,
					status = PendingMutationStatus.Pending
				)
			)
		)
		val repository = PendingChangesDataSource(
			pendingMutationDao = pendingMutationDao,
			academicRecordRepository = FakeAcademicRecordRepository(),
			evaluationRepository = FakeEvaluationRepository(),
			syncStatusRepository = FakeSyncStatusRepository()
		)

		val actual = repository.getPendingChanges()

		assertEquals(3, actual.totalCount)
		assertEquals(2, actual.recordCount)
		assertEquals(1, actual.evaluationsCount)
		assertEquals(true, actual.hasFailedMutations)
	}

	@Test
	fun flushPendingChanges_retriesAndDrainsRecordBeforeEvaluations() = runTest {
		val operations = mutableListOf<String>()
		val pendingMutationDao = FakePendingMutationDao(
			mutations = mutableListOf(
				pendingMutation(
					mutationId = "record-failed",
					storeId = RECORD_MUTATION_STORE_ID,
					scopeKey = RECORD_MUTATION_SCOPE,
					status = PendingMutationStatus.Failed
				),
				pendingMutation(
					mutationId = "evaluations-pending",
					storeId = EVALUATIONS_MUTATION_STORE_ID,
					scopeKey = EVALUATIONS_MUTATION_SCOPE,
					status = PendingMutationStatus.Pending
				)
			),
			operations = operations
		)
		val repository = PendingChangesDataSource(
			pendingMutationDao = pendingMutationDao,
			academicRecordRepository = FakeAcademicRecordRepository(
				onDrainPendingMutations = {
					operations += "drain:$RECORD_MUTATION_SCOPE"
					pendingMutationDao.clearScope(
						storeId = RECORD_MUTATION_STORE_ID,
						scopeKey = RECORD_MUTATION_SCOPE
					)
				}
			),
			evaluationRepository = FakeEvaluationRepository(
				onDrainPendingMutations = {
					operations += "drain:$EVALUATIONS_MUTATION_SCOPE"
					pendingMutationDao.clearScope(
						storeId = EVALUATIONS_MUTATION_STORE_ID,
						scopeKey = EVALUATIONS_MUTATION_SCOPE
					)
				}
			),
			syncStatusRepository = FakeSyncStatusRepository(initialValue = SyncStatus.Healthy)
		)

		val actual = repository.flushPendingChanges()

		assertEquals(FlushPendingChangesResult.Success, actual)
		assertEquals(
			listOf(
				"retry:$RECORD_MUTATION_SCOPE",
				"drain:$RECORD_MUTATION_SCOPE",
				"retry:$EVALUATIONS_MUTATION_SCOPE",
				"drain:$EVALUATIONS_MUTATION_SCOPE"
			),
			operations
		)
		assertEquals(0, repository.getPendingChanges().totalCount)
	}
}

private class FakePendingMutationDao(
	private val mutations: MutableList<PendingMutationEntity>,
	private val operations: MutableList<String> = mutableListOf()
) : PendingMutationDao() {
	override suspend fun upsertEntity(entity: PendingMutationEntity) {
		deletePendingMutation(
			storeId = entity.storeId,
			scopeKey = entity.scopeKey,
			mutationId = entity.mutationId
		)
		mutations += entity
	}

	override suspend fun upsertEntities(entities: List<PendingMutationEntity>) {
		entities.forEach(::upsertEntity)
	}

	override fun observePendingMutations(
		storeId: String,
		scopeKey: String
	): Flow<List<PendingMutationEntity>> {
		return flowOf(
			mutations.filter { mutation ->
				mutation.storeId == storeId &&
					mutation.scopeKey == scopeKey &&
					mutation.status == PendingMutationStatus.Pending.name
			}
		)
	}

	override suspend fun getPendingMutations(
		storeId: String,
		scopeKey: String
	): List<PendingMutationEntity> {
		return mutations.filter { mutation ->
			mutation.storeId == storeId &&
				mutation.scopeKey == scopeKey &&
				mutation.status == PendingMutationStatus.Pending.name
		}
	}

	override suspend fun getMutations(storeId: String, scopeKey: String): List<PendingMutationEntity> {
		return mutations.filter { mutation ->
			mutation.storeId == storeId && mutation.scopeKey == scopeKey
		}
	}

	override suspend fun getPendingMutation(
		storeId: String,
		scopeKey: String,
		mutationId: String
	): PendingMutationEntity? {
		return mutations.firstOrNull { mutation ->
			mutation.storeId == storeId &&
				mutation.scopeKey == scopeKey &&
				mutation.mutationId == mutationId
		}
	}

	override suspend fun getPendingMutation(mutationId: String): PendingMutationEntity? {
		return mutations.firstOrNull { mutation -> mutation.mutationId == mutationId }
	}

	override suspend fun deletePendingMutation(storeId: String, scopeKey: String, mutationId: String): Int {
		return if (mutations.removeAll { mutation ->
				mutation.storeId == storeId &&
					mutation.scopeKey == scopeKey &&
					mutation.mutationId == mutationId
			}
		) {
			1
		} else {
			0
		}
	}

	override suspend fun deletePendingMutationsByReplaceKey(storeId: String, replaceKey: String): Int {
		val sizeBefore = mutations.size
		mutations.removeAll { mutation ->
			mutation.storeId == storeId && mutation.replaceKey == replaceKey
		}
		return sizeBefore - mutations.size
	}

	override suspend fun retryFailedMutations(
		storeId: String,
		scopeKey: String,
		status: String,
		updatedAt: Long
	): Int {
		operations += "retry:$scopeKey"
		var retried = 0
		mutations.replaceAll { mutation ->
			if (
				mutation.storeId == storeId &&
				mutation.scopeKey == scopeKey &&
				mutation.status == PendingMutationStatus.Failed.name
			) {
				retried++
				mutation.copy(
					status = status,
					updatedAt = updatedAt,
					lastError = null
				)
			} else {
				mutation
			}
		}
		return retried
	}

	override suspend fun deleteAll(): Int {
		val size = mutations.size
		mutations.clear()
		return size
	}

	fun clearScope(storeId: String, scopeKey: String) {
		mutations.removeAll { mutation ->
			mutation.storeId == storeId && mutation.scopeKey == scopeKey
		}
	}
}

private class FakeAcademicRecordRepository(
	private val onDrainPendingMutations: suspend () -> Unit = {}
) : AcademicRecordRepository {
	override suspend fun observeAcademicRecordFlow(): Flow<AcademicRecord> = emptyFlow()

	override suspend fun getAcademicRecord(): AcademicRecord? = null

	override suspend fun updateAcademicRecord() = Unit

	override suspend fun drainPendingMutations() {
		onDrainPendingMutations()
	}

	override suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?,
		commit: Boolean
	) = Unit

	override suspend fun deleteAttemptOverride(attemptId: String) = Unit

	override suspend fun addSyntheticTerm(command: AcademicRecordMutation.AddSyntheticTerm) = Unit

	override suspend fun deleteSyntheticTerm(termId: String) = Unit
}

private class FakeEvaluationRepository(
	private val onDrainPendingMutations: suspend () -> Unit = {}
) : EvaluationRepository {
	override suspend fun observeEvaluationsFlow(): Flow<List<Evaluation>> = emptyFlow()

	override suspend fun observeHasSyncedEvaluationsFlow(): Flow<Boolean> = flowOf(false)

	override suspend fun updateEvaluations() = Unit

	override suspend fun drainPendingMutations() {
		onDrainPendingMutations()
	}

	override suspend fun getEvaluation(eid: String) = null

	override suspend fun addEvaluation(add: EvaluationAdd) = Unit

	override suspend fun updateEvaluation(update: EvaluationUpdate) = Unit

	override suspend fun removeEvaluation(remove: EvaluationRemove) = Unit

	override suspend fun getAvailableAttempts(): List<EditableAttemptDescriptor> = emptyList()
}

private fun pendingMutation(
	mutationId: String,
	storeId: String,
	scopeKey: String,
	status: PendingMutationStatus
): PendingMutationEntity {
	return PendingMutationEntity(
		mutationId = mutationId,
		storeId = storeId,
		scopeKey = scopeKey,
		entityType = "$scopeKey:entity",
		entityId = mutationId,
		replaceKey = "$scopeKey:$mutationId",
		payload = "{}",
		preconditionType = "revision",
		expectedRevision = 0L,
		status = status.name,
		createdAt = 1L,
		updatedAt = 1L,
		lastError = if (status == PendingMutationStatus.Failed) "boom" else null
	)
}
