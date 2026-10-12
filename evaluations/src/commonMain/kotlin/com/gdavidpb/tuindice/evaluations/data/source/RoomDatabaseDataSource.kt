package com.gdavidpb.tuindice.evaluations.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.GradingMode
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.evaluations.data.mapper.toEvaluationEntity
import com.gdavidpb.tuindice.evaluations.data.mapper.toLocalCurrentTermDescriptor
import com.gdavidpb.tuindice.evaluations.data.mapper.toLocalEditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.data.mapper.toLocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.LocalCurrentTermDescriptor
import com.gdavidpb.tuindice.evaluations.data.model.LocalEditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluation
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
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicTermEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.EvaluationSyncStateEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.currentTermEntityOrNull
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.StoreBackedMutationEngine
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceTransactionRunner
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RoomDatabaseDataSource(
	private val evaluationDao: EvaluationDao,
	private val evaluationSyncStateDao: EvaluationSyncStateDao,
	private val academicTermDao: AcademicTermDao,
	private val academicAttemptDao: AcademicAttemptDao,
	private val transactionRunner: PersistenceTransactionRunner,
	private val mutationEngine: StoreBackedMutationEngine<String, EvaluationMutation, EvaluationMutationAck>,
	private val visibleEvaluationsStateResolver: VisibleEvaluationsStateResolver
) : DatabaseDataRepository, SessionMemory {
	private val writeMutex = Mutex()

	private var inMemoryConfirmedSnapshot: LocalEvaluationsSnapshot? = null
	private var pendingMutationsSnapshot: List<MutationEnvelope<String, EvaluationMutation>>? = null

	override fun observeEvaluationsFlow(): Flow<List<LocalEvaluation>> {
		return observeEvaluationsSnapshotFlow()
			.map { snapshot -> snapshot.evaluations }
	}

	override fun observeEvaluationsSnapshotFlow(): Flow<LocalEvaluationsSnapshot> {
		val pendingFlow = mutationEngine.observeMutations(EVALUATIONS_MUTATION_SCOPE)
			.onEach { mutations ->
				pendingMutationsSnapshot = mutations
			}

		return combine(observeConfirmedSnapshotFlow(), pendingFlow) { confirmedSnapshot, pendingMutations ->
			confirmedSnapshot.copy(
				evaluations = visibleEvaluationsStateResolver.resolveVisibleState(
					confirmedSnapshot = confirmedSnapshot,
					pendingMutations = pendingMutations
				)
			)
		}
	}

	override fun observeHasSyncedEvaluationsFlow(): Flow<Boolean> {
		return evaluationSyncStateDao.observeSyncState()
			.map { syncState -> syncState?.hasSynced == true }
	}

	private fun observeConfirmedSnapshotFlow(): Flow<LocalEvaluationsSnapshot> {
		return combine(
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
	}

	// Under the write lock so a write in flight cannot put back the snapshot being dropped. The
	// next read loads from Room, which the wipe has just emptied.
	override suspend fun clearSessionMemory() {
		writeMutex.withLock {
			inMemoryConfirmedSnapshot = null
			pendingMutationsSnapshot = null
		}
	}

	override suspend fun getEvaluation(eid: String): LocalEvaluation? {
		return getEvaluationsSnapshot().evaluations
			.firstOrNull { evaluation -> evaluation.id == eid }
	}

	override suspend fun getEvaluationsSnapshot(): LocalEvaluationsSnapshot {
		val confirmedSnapshot = getConfirmedSnapshot()
		return confirmedSnapshot.copy(
			evaluations = visibleEvaluationsStateResolver.resolveVisibleState(
				confirmedSnapshot = confirmedSnapshot,
				pendingMutations = currentPendingMutations()
			)
		)
	}

	override suspend fun getConfirmedSnapshot(): LocalEvaluationsSnapshot {
		return inMemoryConfirmedSnapshot ?: loadSnapshotFromRoom()
	}

	override suspend fun getAvailableAttempts(): List<LocalEditableAttemptDescriptor> {
		return selectAvailableAttempts(
			terms = academicTermDao.getTerms(),
			attempts = academicAttemptDao.getAttempts()
		)
	}

	override fun observeAvailableAttemptsFlow(): Flow<List<LocalEditableAttemptDescriptor>> {
		return observeAvailableAttempts(
			changes = merge(
				academicTermDao.observeTermsFlow(),
				academicAttemptDao.observeAttemptsFlow()
			),
			readTerms = academicTermDao::getTerms,
			readAttempts = academicAttemptDao::getAttempts
		)
	}

	override suspend fun getCurrentTerm() =
		selectOfficialCurrentTerm(
			terms = academicTermDao.getTerms()
		)?.toLocalCurrentTermDescriptor()

	override fun observeCurrentTermFlow(): Flow<LocalCurrentTermDescriptor?> {
		return academicTermDao.observeTermsFlow()
			.map { terms -> selectOfficialCurrentTerm(terms = terms)?.toLocalCurrentTermDescriptor() }
			.distinctUntilChanged()
	}

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

	override suspend fun confirmEvaluationRemoval(eid: String) {
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

	override suspend fun discardLocalEvaluationCopy(eid: String) {
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
		return pendingMutationsSnapshot
			?: mutationEngine.getMutations(EVALUATIONS_MUTATION_SCOPE)
	}

	internal companion object {
		// Each emission of [changes] only says "the terms or the attempts changed": both are read again
		// afterwards, so a record write that moves the term and its attempts in one transaction yields
		// the new term with its own attempts, never the new term over the old attempts. A write landing
		// between the two reads emits again and corrects it. Never twice the same list.
		@OptIn(ExperimentalCoroutinesApi::class)
		fun observeAvailableAttempts(
			changes: Flow<Any?>,
			readTerms: suspend () -> List<AcademicTermEntity>,
			readAttempts: suspend () -> List<AcademicAttemptEntity>
		): Flow<List<LocalEditableAttemptDescriptor>> {
			return changes
				.mapLatest {
					selectAvailableAttempts(
						terms = readTerms(),
						attempts = readAttempts()
					)
				}
				.distinctUntilChanged()
		}

		fun selectAvailableAttempts(
			terms: List<AcademicTermEntity>,
			attempts: List<AcademicAttemptEntity>
		): List<LocalEditableAttemptDescriptor> {
			val currentTermId = selectOfficialCurrentTermId(terms = terms)
				?: return emptyList()

			return attempts
				.asSequence()
				.filter { attempt -> attempt.termId == currentTermId }
				.filter { attempt -> attempt.gradingMode == "NUMERIC" }
				.map { attempt -> attempt.toLocalEditableAttemptDescriptor() }
				.filter { attempt -> attempt.gradingMode == GradingMode.NUMERIC }
				.sortedBy(LocalEditableAttemptDescriptor::code)
				.toList()
		}

		fun selectOfficialCurrentTermId(
			terms: List<AcademicTermEntity>
		): String? = selectOfficialCurrentTerm(terms = terms)?.id

		fun selectOfficialCurrentTerm(
			terms: List<AcademicTermEntity>
		): AcademicTermEntity? = terms.currentTermEntityOrNull()
	}
}
