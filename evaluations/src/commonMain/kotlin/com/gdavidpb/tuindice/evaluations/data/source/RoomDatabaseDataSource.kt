package com.gdavidpb.tuindice.evaluations.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.evaluations.data.mapper.toLocalCurrentTermDescriptor
import com.gdavidpb.tuindice.evaluations.data.mapper.toEvaluationEntity
import com.gdavidpb.tuindice.evaluations.data.mapper.toLocalEditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.data.mapper.toLocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.LocalEditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.mutation.EVALUATIONS_MUTATION_SCOPE
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutation
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutationAck
import com.gdavidpb.tuindice.evaluations.data.repository.DatabaseDataRepository
import com.gdavidpb.tuindice.evaluations.data.resolver.VisibleEvaluationsStateResolver
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicAttemptDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicTermDao
import com.gdavidpb.tuindice.persistence.data.room.daos.EvaluationDao
import com.gdavidpb.tuindice.persistence.data.room.daos.EvaluationSyncStateDao
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicTermEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.EvaluationSyncStateEntity
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.StoreBackedMutationEngine
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceTransactionRunner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RoomDatabaseDataSource(
	private val evaluationDao: EvaluationDao,
	private val evaluationSyncStateDao: EvaluationSyncStateDao,
	private val academicTermDao: AcademicTermDao,
	private val academicAttemptDao: AcademicAttemptDao,
	private val transactionRunner: PersistenceTransactionRunner,
	private val mutationEngine: StoreBackedMutationEngine<String, EvaluationMutation, LocalEvaluationsSnapshot, List<LocalEvaluation>, EvaluationMutationAck>,
	private val visibleEvaluationsStateResolver: VisibleEvaluationsStateResolver
) : DatabaseDataRepository {
	private val writeMutex = Mutex()

	private var inMemoryConfirmedSnapshot: LocalEvaluationsSnapshot? = null
	private var pendingMutationsSnapshot: List<MutationEnvelope<String, EvaluationMutation>> = emptyList()

	override fun observeEvaluationsFlow(): Flow<List<LocalEvaluation>> {
		val confirmedFlow = combine(
			evaluationDao.observeEvaluationsFlow(),
			evaluationSyncStateDao.observeSyncState()
		) { evaluations, syncState ->
			LocalEvaluationsSnapshot(
				hasSynced = syncState?.hasSynced == true,
				evaluations = evaluations.map { evaluation -> evaluation.toLocalEvaluation() }
			)
		}.onEach { snapshot ->
			inMemoryConfirmedSnapshot = snapshot
		}

		val pendingFlow = mutationEngine.observePendingMutations(EVALUATIONS_MUTATION_SCOPE)
			.onEach { mutations ->
				pendingMutationsSnapshot = mutations
			}

		return combine(confirmedFlow, pendingFlow) { confirmedSnapshot, pendingMutations ->
			visibleEvaluationsStateResolver.resolveVisibleState(
				confirmedSnapshot = confirmedSnapshot,
				pendingMutations = pendingMutations
			)
		}
	}

	override fun observeHasSyncedEvaluationsFlow(): Flow<Boolean> {
		return evaluationSyncStateDao.observeSyncState()
			.map { syncState -> syncState?.hasSynced == true }
	}

	override suspend fun getEvaluation(eid: String): LocalEvaluation? {
		val confirmedSnapshot = getConfirmedSnapshot()
		return visibleEvaluationsStateResolver.resolveVisibleState(
			confirmedSnapshot = confirmedSnapshot,
			pendingMutations = currentPendingMutations()
		).firstOrNull { evaluation -> evaluation.id == eid }
	}

	override suspend fun getConfirmedSnapshot(): LocalEvaluationsSnapshot {
		return inMemoryConfirmedSnapshot ?: loadSnapshotFromRoom()
	}

	override suspend fun getAvailableAttempts(): List<LocalEditableAttemptDescriptor> {
		val currentTermId = selectOfficialCurrentTermId(
			terms = academicTermDao.getTerms(),
			nowMillis = currentTimeMillis()
		) ?: return emptyList()

		return academicAttemptDao
			.getAttempts()
			.asSequence()
			.filter { attempt -> attempt.termId == currentTermId }
			.filter { attempt -> attempt.gradingMode == "NUMERIC" }
			.map { attempt -> attempt.toLocalEditableAttemptDescriptor() }
			.filter { attempt -> attempt.gradingMode == GradingMode.NUMERIC }
			.sortedBy(LocalEditableAttemptDescriptor::code)
			.toList()
	}

	override suspend fun getCurrentTerm() =
		selectOfficialCurrentTerm(
			terms = academicTermDao.getTerms(),
			nowMillis = currentTimeMillis()
		)?.toLocalCurrentTermDescriptor()

	override suspend fun confirmAddedEvaluation(
		evaluation: LocalEvaluation
	): LocalEvaluation {
		writeMutex.withLock {
			val currentSnapshot = getConfirmedSnapshot()
			val mergedEvaluations = currentSnapshot.evaluations
				.filterNot { current ->
					current.id == evaluation.id || current.referenceId == evaluation.referenceId
				} + evaluation

			persistConfirmedSnapshot(
				snapshot = currentSnapshot.copy(
					hasSynced = true,
					evaluations = mergedEvaluations
				),
				replaceAll = false
			)
		}

		return evaluation
	}

	override suspend fun confirmUpdatedEvaluation(
		evaluation: LocalEvaluation
	): LocalEvaluation {
		writeMutex.withLock {
			val currentSnapshot = getConfirmedSnapshot()
			val mergedEvaluations = if (
				currentSnapshot.evaluations.any { current -> current.id == evaluation.id }
			) {
				currentSnapshot.evaluations.map { current ->
					if (current.id == evaluation.id) evaluation else current
				}
			} else {
				currentSnapshot.evaluations + evaluation
			}

			persistConfirmedSnapshot(
				snapshot = currentSnapshot.copy(
					hasSynced = true,
					evaluations = mergedEvaluations
				),
				replaceAll = false
			)
		}

		return evaluation
	}

	override suspend fun confirmRemovedEvaluation(eid: String) {
		writeMutex.withLock {
			val currentSnapshot = getConfirmedSnapshot()
			transactionRunner.immediate {
				evaluationDao.deleteEvaluation(eid)
				evaluationSyncStateDao.upsertEntity(
					EvaluationSyncStateEntity(hasSynced = true)
				)
			}

			inMemoryConfirmedSnapshot = currentSnapshot.copy(
				hasSynced = true,
				evaluations = currentSnapshot.evaluations.filterNot { evaluation -> evaluation.id == eid }
			)
		}
	}

	override suspend fun removeConfirmedEvaluation(eid: String) {
		writeMutex.withLock {
			val currentSnapshot = getConfirmedSnapshot()
			evaluationDao.deleteEvaluation(eid)
			inMemoryConfirmedSnapshot = currentSnapshot.copy(
				evaluations = currentSnapshot.evaluations.filterNot { evaluation -> evaluation.id == eid }
			)
		}
	}

	override suspend fun saveConfirmedSnapshot(snapshot: LocalEvaluationsSnapshot) {
		writeMutex.withLock {
			persistConfirmedSnapshot(
				snapshot = snapshot,
				replaceAll = true
			)
		}
	}

	private suspend fun loadSnapshotFromRoom(): LocalEvaluationsSnapshot {
		val evaluations = evaluationDao.observeEvaluationsFlow()
			.map { items -> items.map { item -> item.toLocalEvaluation() } }
		return LocalEvaluationsSnapshot(
			hasSynced = evaluationSyncStateDao.getSyncState()?.hasSynced == true,
			evaluations = evaluations.first()
		).also { snapshot ->
			inMemoryConfirmedSnapshot = snapshot
		}
	}

	private suspend fun persistConfirmedSnapshot(
		snapshot: LocalEvaluationsSnapshot,
		replaceAll: Boolean
	) {
		val entities = snapshot.evaluations.map { evaluation -> evaluation.toEvaluationEntity() }

		transactionRunner.immediate {
			if (replaceAll) {
				evaluationDao.deleteAll()
			}
			evaluationDao.upsertEntities(entities)
			evaluationSyncStateDao.upsertEntity(
				EvaluationSyncStateEntity(hasSynced = snapshot.hasSynced)
			)
		}

		inMemoryConfirmedSnapshot = snapshot.copy(
			evaluations = snapshot.evaluations.sortedBy { evaluation -> evaluation.date ?: Long.MAX_VALUE }
		)
	}

	private suspend fun currentPendingMutations(): List<MutationEnvelope<String, EvaluationMutation>> {
		return pendingMutationsSnapshot.ifEmpty {
			mutationEngine.getPendingMutations(EVALUATIONS_MUTATION_SCOPE)
		}
	}

	internal companion object {
		fun selectOfficialCurrentTermId(
			terms: List<AcademicTermEntity>,
			nowMillis: Long
		): String? = selectOfficialCurrentTerm(
			terms = terms,
			nowMillis = nowMillis
		)?.id

		fun selectOfficialCurrentTerm(
			terms: List<AcademicTermEntity>,
			@Suppress("UNUSED_PARAMETER")
			nowMillis: Long
		): AcademicTermEntity? {
			val termComparator = compareBy(
				AcademicTermEntity::termOrder,
				AcademicTermEntity::id
			)

			return terms
				.filter { term -> TermKind.valueOf(term.kind) == TermKind.CURRENT }
				.maxWithOrNull(termComparator)
		}
	}
}
