package com.gdavidpb.tuindice.evaluations.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.academiccore.domain.model.isEditable
import com.gdavidpb.tuindice.base.domain.model.subject.GradingMode
import com.gdavidpb.tuindice.evaluations.data.mapper.toEvaluationEntity
import com.gdavidpb.tuindice.evaluations.data.mapper.toLocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.mapper.toLocalSubject
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.model.LocalSubject
import com.gdavidpb.tuindice.evaluations.data.mutation.EVALUATIONS_MUTATION_SCOPE
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutation
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutationAck
import com.gdavidpb.tuindice.evaluations.data.repository.DatabaseDataRepository
import com.gdavidpb.tuindice.evaluations.data.resolver.VisibleEvaluationsStateResolver
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.entity.EvaluationSyncStateEntity
import com.gdavidpb.tuindice.persistence.data.room.withImmediateTransaction
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.StoreBackedMutationEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val CURRENT_ACADEMIC_RECORD_ID = "self"
private const val OFFICIAL_VIEW_MODE = "official"

class RoomDatabaseDataSource(
	private val room: TuIndiceDatabase,
	private val mutationEngine: StoreBackedMutationEngine<String, EvaluationMutation, LocalEvaluationsSnapshot, List<LocalEvaluation>, EvaluationMutationAck>,
	private val visibleEvaluationsStateResolver: VisibleEvaluationsStateResolver
) : DatabaseDataRepository {
	private val writeMutex = Mutex()

	private var inMemoryConfirmedSnapshot: LocalEvaluationsSnapshot? = null
	private var pendingMutationsSnapshot: List<MutationEnvelope<String, EvaluationMutation>> = emptyList()

	override fun observeEvaluationsFlow(): Flow<List<LocalEvaluation>> {
		val confirmedFlow = combine(
			room.evaluations.observeEvaluationsFlow(),
			room.evaluationSyncState.observeSyncState()
		) { evaluations, syncState ->
			LocalEvaluationsSnapshot(
				anchorRevision = syncState?.anchorRevision ?: 0L,
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

	override suspend fun getAvailableSubjects(): List<LocalSubject> {
		val openTermIds = room.academicTermProjections
			.getTerms(CURRENT_ACADEMIC_RECORD_ID, OFFICIAL_VIEW_MODE)
			.filter { term -> isEditableTermKind(term.kind) }
			.mapTo(hashSetOf()) { term -> term.id }

		return room.academicAttemptProjections
			.getAttempts(CURRENT_ACADEMIC_RECORD_ID, OFFICIAL_VIEW_MODE)
			.asSequence()
			.filter { attempt -> attempt.termId in openTermIds }
			.filter { attempt -> attempt.gradingMode == "NUMERIC" }
			.map { attempt -> attempt.toLocalSubject() }
			.filter { subject -> subject.gradingMode == GradingMode.NUMERIC }
			.sortedBy(LocalSubject::code)
			.toList()
	}

	override suspend fun confirmAddedEvaluation(
		evaluation: LocalEvaluation,
		anchorRevision: Long
	): LocalEvaluation {
		writeMutex.withLock {
			val currentSnapshot = getConfirmedSnapshot()
			val mergedEvaluations = currentSnapshot.evaluations
				.filterNot { current ->
					current.id == evaluation.id || current.referenceId == evaluation.referenceId
				} + evaluation

			persistConfirmedSnapshot(
				snapshot = currentSnapshot.copy(
					anchorRevision = anchorRevision,
					evaluations = mergedEvaluations
				),
				replaceAll = false
			)
		}

		return evaluation
	}

	override suspend fun confirmUpdatedEvaluation(
		evaluation: LocalEvaluation,
		anchorRevision: Long
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
					anchorRevision = anchorRevision,
					evaluations = mergedEvaluations
				),
				replaceAll = false
			)
		}

		return evaluation
	}

	override suspend fun confirmRemovedEvaluation(eid: String, anchorRevision: Long) {
		writeMutex.withLock {
			val currentSnapshot = getConfirmedSnapshot()
			room.withImmediateTransaction {
				room.evaluations.deleteEvaluation(eid)
				room.evaluationSyncState.upsertEntity(
					EvaluationSyncStateEntity(anchorRevision = anchorRevision)
				)
			}

			inMemoryConfirmedSnapshot = currentSnapshot.copy(
				anchorRevision = anchorRevision,
				evaluations = currentSnapshot.evaluations.filterNot { evaluation -> evaluation.id == eid }
			)
		}
	}

	override suspend fun removeConfirmedEvaluation(eid: String) {
		writeMutex.withLock {
			val currentSnapshot = getConfirmedSnapshot()
			room.evaluations.deleteEvaluation(eid)
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
		val evaluations = room.evaluations.observeEvaluationsFlow()
			.map { items -> items.map { item -> item.toLocalEvaluation() } }
		return LocalEvaluationsSnapshot(
			anchorRevision = room.evaluationSyncState.getSyncState()?.anchorRevision ?: 0L,
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

		room.withImmediateTransaction {
			if (replaceAll) {
				room.evaluations.deleteAll()
			}
			room.evaluations.upsertEntities(entities)
			room.evaluationSyncState.upsertEntity(
				EvaluationSyncStateEntity(anchorRevision = snapshot.anchorRevision)
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
		fun isEditableTermKind(kind: String): Boolean {
			return TermKind.valueOf(kind).isEditable
		}
	}
}
