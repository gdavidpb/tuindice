package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.withImmediateTransaction
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.StoreBackedMutationEngine
import com.gdavidpb.tuindice.record.data.model.SubjectGradePreview
import com.gdavidpb.tuindice.record.data.model.SubjectPreviewKey
import com.gdavidpb.tuindice.record.data.repository.QuarterLocalDataRepository
import com.gdavidpb.tuindice.record.data.resolver.VisibleRecordStateResolver
import com.gdavidpb.tuindice.record.data.mutation.RECORD_MUTATION_SCOPE
import com.gdavidpb.tuindice.record.data.mutation.RecordMutationAck
import com.gdavidpb.tuindice.record.data.mutation.RecordMutation
import com.gdavidpb.tuindice.record.data.model.quarter.LocalQuarter
import com.gdavidpb.tuindice.record.data.model.quarter.LocalSubject
import com.gdavidpb.tuindice.record.data.model.quarter.SetSubjectGradeResult
import com.gdavidpb.tuindice.record.data.source.database.mapper.toLocalQuarter
import com.gdavidpb.tuindice.record.data.source.database.mapper.toQuarterEntity
import com.gdavidpb.tuindice.record.data.source.database.mapper.toSubjectEntity
import com.gdavidpb.tuindice.record.domain.service.IndexComputationEngine
import com.gdavidpb.tuindice.record.domain.service.SimulationProjectionEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RoomDataSource(
	private val room: TuIndiceDatabase,
	private val indexComputationEngine: IndexComputationEngine,
	private val simulationProjectionEngine: SimulationProjectionEngine,
	private val mutationEngine: StoreBackedMutationEngine<String, RecordMutation, List<LocalQuarter>, List<LocalQuarter>, RecordMutationAck>,
	private val visibleRecordStateResolver: VisibleRecordStateResolver
) : QuarterLocalDataRepository {
	private val writeMutex = Mutex()
	private val previewQuartersFlow = MutableStateFlow(0L)

	private var inMemoryQuartersSnapshot: List<LocalQuarter>? = null
	private var pendingMutationsSnapshot: List<MutationEnvelope<String, RecordMutation>> = emptyList()
	private var gradePreviewSnapshot: Map<SubjectPreviewKey, SubjectGradePreview> = emptyMap()

	override fun getQuartersFlow(): Flow<List<LocalQuarter>> {
		val quartersFlow = room.quarters.getQuartersWithSubjectsFlow()
			.map { quarters ->
				quarters
					.map { quarter -> quarter.toLocalQuarter() }
					.toCanonicalOrder()
			}
			.onEach { quarters ->
				inMemoryQuartersSnapshot = quarters
			}

		val pendingFlow = mutationEngine.observePendingMutations(RECORD_MUTATION_SCOPE)
			.onEach { pendingMutations ->
				pendingMutationsSnapshot = pendingMutations
			}

		return combine(quartersFlow, pendingFlow, previewQuartersFlow) { confirmedQuarters, pendingMutations, _ ->
			visibleRecordStateResolver.resolveVisibleState(
				confirmedSnapshot = confirmedQuarters,
				pendingMutations = pendingMutations,
				gradePreviewSnapshot = gradePreviewSnapshot
			)
		}
	}

	override suspend fun getConfirmedQuarters(): List<LocalQuarter> {
		return inMemoryQuartersSnapshot ?: loadSnapshotFromRoom()
	}

	override suspend fun getQuarter(qid: String): LocalQuarter? {
		val confirmedSnapshot = inMemoryQuartersSnapshot ?: loadSnapshotFromRoom()
		val pendingMutations = currentPendingMutations()
		val visibleSnapshot = visibleRecordStateResolver.resolveVisibleState(
			confirmedSnapshot = confirmedSnapshot,
			pendingMutations = pendingMutations,
			gradePreviewSnapshot = gradePreviewSnapshot
		)

		return visibleSnapshot.firstOrNull { quarter -> quarter.id == qid }
	}

	override suspend fun confirmQuarterAddition(
		addedQuarter: LocalQuarter,
		affectedQuarters: List<LocalQuarter>
	) {
		writeMutex.withLock {
			val currentSnapshot = inMemoryQuartersSnapshot ?: loadSnapshotFromRoom()
			val mergedSnapshot = mergeSubjectGradeAckIntoSnapshot(
				currentSnapshot = currentSnapshot,
				affectedQuarters = buildList {
					add(addedQuarter)
					affectedQuarters.forEach { quarter ->
						if (none { current -> current.id == quarter.id }) {
							add(quarter)
						}
					}
				}.toCanonicalOrder()
			)

			persistConfirmedQuarters(
				confirmedQuarters = mergedSnapshot,
				currentSnapshot = currentSnapshot
			)
		}
	}

	override suspend fun removeQuarter(qid: String) {
		writeMutex.withLock {
			room.quarters.deleteQuarter(qid = qid)

			inMemoryQuartersSnapshot = inMemoryQuartersSnapshot
				?.filterNot { quarter -> quarter.id == qid }

			removeGradePreviews { key, _ -> key.quarterId == qid }
		}
	}

	override suspend fun confirmQuarterRemoval(qid: String, affectedQuarters: List<LocalQuarter>) {
		writeMutex.withLock {
			val quarterEntities = affectedQuarters
				.map { quarter -> quarter.toQuarterEntity() }
			val subjectEntities = affectedQuarters
				.flatMap { quarter -> quarter.subjects }
				.map { subject -> subject.toSubjectEntity() }

			room.withImmediateTransaction {
				room.quarters.upsertEntities(quarterEntities)
				room.subjects.upsertEntities(subjectEntities)
				room.quarters.deleteQuarter(qid = qid)
			}

			inMemoryQuartersSnapshot = inMemoryQuartersSnapshot
				.orEmpty()
				.filterNot { quarter -> quarter.id == qid }
				.let { current -> mergePersistedQuarters(current = current, updates = affectedQuarters) }

			removeGradePreviews { key, _ -> key.quarterId == qid }
		}
	}

	override suspend fun confirmSubjectGradeMutation(affectedQuarters: List<LocalQuarter>) {
		writeMutex.withLock {
			if (affectedQuarters.isEmpty()) return@withLock

			val currentSnapshot = inMemoryQuartersSnapshot ?: loadSnapshotFromRoom()
			val mergedSnapshot = mergeSubjectGradeAckIntoSnapshot(
				currentSnapshot = currentSnapshot,
				affectedQuarters = affectedQuarters.toCanonicalOrder()
			)

			persistConfirmedQuarters(
				confirmedQuarters = mergedSnapshot,
				currentSnapshot = currentSnapshot
			)
		}
	}

	override suspend fun saveQuarters(quarters: List<LocalQuarter>) {
		writeMutex.withLock {
			persistConfirmedQuarters(
				confirmedQuarters = quarters.toCanonicalOrder(),
				currentSnapshot = inMemoryQuartersSnapshot ?: loadSnapshotFromRoom()
			)
		}
	}

	override suspend fun saveSubjects(subjects: List<LocalSubject>) {
		val subjectEntities = subjects.map { subject -> subject.toSubjectEntity() }
		val updatedSubjectIds = subjects.mapTo(hashSetOf()) { subject -> subject.id }

		writeMutex.withLock {
			room.subjects.upsertEntities(subjectEntities)

			val updatesById = subjects.associateBy { subject -> subject.id }

			inMemoryQuartersSnapshot = inMemoryQuartersSnapshot
				?.map { quarter ->
					quarter.copy(
						subjects = quarter.subjects.map { subject ->
							updatesById[subject.id] ?: subject
						}
					)
				}

			removeGradePreviews { key, _ -> key.subjectId in updatedSubjectIds }
		}
	}

	override suspend fun clearSubjectGradePreview(qid: String, sid: String) {
		writeMutex.withLock {
			val key = SubjectPreviewKey(
				quarterId = qid,
				subjectId = sid
			)

			removeGradePreview(key)
		}
	}

	override suspend fun setSubjectGradeAndRecompute(
		qid: String,
		sid: String,
		grade: Int,
		commit: Boolean
	): SetSubjectGradeResult {
		return writeMutex.withLock {
			val confirmedSnapshot = inMemoryQuartersSnapshot ?: loadSnapshotFromRoom()
			val sourceQuarter = confirmedSnapshot
				.firstOrNull { quarter -> quarter.id == qid }
				?: return@withLock SetSubjectGradeResult.TargetNotFound
			val sourceSubject = sourceQuarter.subjects
				.firstOrNull { subject -> subject.id == sid }
				?: return@withLock SetSubjectGradeResult.TargetNotFound
			val key = SubjectPreviewKey(
				quarterId = qid,
				subjectId = sid
			)

			if (!commit) {
				upsertGradePreview(
					key = key,
					requestedGrade = grade
				)

				val previewSnapshot = visibleRecordStateResolver.resolveVisibleState(
					confirmedSnapshot = confirmedSnapshot,
					pendingMutations = currentPendingMutations(),
					gradePreviewSnapshot = gradePreviewSnapshot
				)

				return@withLock SetSubjectGradeResult.Applied(
					updatedQuarters = previewSnapshot,
					updatedTargetQuarter = previewSnapshot
						.firstOrNull { quarter -> quarter.id == qid }
						?: sourceQuarter,
					expectedRevision = sourceSubject.revision
				)
			}

			removeGradePreview(key)

			if (sourceSubject.grade == grade) {
				return@withLock SetSubjectGradeResult.Applied(
					updatedQuarters = emptyList(),
					updatedTargetQuarter = sourceQuarter,
					expectedRevision = sourceSubject.revision
				)
			}

			val recomputed = recomputeSubjectGrade(
				snapshot = confirmedSnapshot,
				qid = qid,
				sid = sid,
				grade = grade
			)

			val quarterEntities = recomputed.quarters
				.map { quarter -> quarter.toQuarterEntity() }
			val subjectEntities = recomputed.quarters
				.flatMap { quarter -> quarter.subjects }
				.map { subject -> subject.toSubjectEntity() }

			room.withImmediateTransaction {
				room.quarters.upsertEntities(quarterEntities)
				room.subjects.upsertEntities(subjectEntities)
			}

			inMemoryQuartersSnapshot = recomputed.quarters.toCanonicalOrder()

			SetSubjectGradeResult.Applied(
				updatedQuarters = recomputed.quarters,
				updatedTargetQuarter = recomputed.quarters.firstOrNull { quarter -> quarter.id == qid }
					?: sourceQuarter,
				expectedRevision = sourceSubject.revision
			)
		}
	}

	private suspend fun loadSnapshotFromRoom(): List<LocalQuarter> {
		val loaded = room.quarters
			.getQuartersWithSubjects()
			.map { quarter -> quarter.toLocalQuarter() }
			.toCanonicalOrder()

		inMemoryQuartersSnapshot = loaded

		return loaded
	}

	private suspend fun currentPendingMutations(): List<MutationEnvelope<String, RecordMutation>> {
		return pendingMutationsSnapshot.ifEmpty {
			mutationEngine.getPendingMutations(RECORD_MUTATION_SCOPE)
		}
	}

	private suspend fun persistConfirmedQuarters(
		confirmedQuarters: List<LocalQuarter>,
		currentSnapshot: List<LocalQuarter>
	) {
		val pendingMutations = currentPendingMutations()
		val syncResolution = visibleRecordStateResolver.resolveIncomingState(
			incomingConfirmedState = confirmedQuarters,
			pendingMutations = pendingMutations
		)
		val incomingQuarterIds = confirmedQuarters
			.mapTo(linkedSetOf()) { quarter -> quarter.id }
		val staleQuarterIds = currentSnapshot
			.mapTo(linkedSetOf()) { quarter -> quarter.id }
			.apply { removeAll(incomingQuarterIds) }
		val quarterEntities = confirmedQuarters
			.map { quarter -> quarter.toQuarterEntity() }
		val subjectEntities = confirmedQuarters
			.flatMap { quarter -> quarter.subjects }
			.map { subject -> subject.toSubjectEntity() }

		room.withImmediateTransaction {
			(staleQuarterIds + syncResolution.replacedClosedQuarterIds).forEach { qid ->
				room.quarters.deleteQuarter(qid = qid)
			}
			room.quarters.upsertEntities(quarterEntities)
			room.subjects.upsertEntities(subjectEntities)
		}

		pendingMutationsSnapshot = syncResolution.compatiblePendingMutations
		syncResolution.invalidatedMutationIds.forEach { mutationId ->
			mutationEngine.deletePendingMutation(RECORD_MUTATION_SCOPE, mutationId)
		}
		removeGradePreviews { key, _ -> key.quarterId in syncResolution.invalidatedQuarterIds }

		inMemoryQuartersSnapshot = confirmedQuarters
	}

	private fun mergeSubjectGradeAckIntoSnapshot(
		currentSnapshot: List<LocalQuarter>,
		affectedQuarters: List<LocalQuarter>
	): List<LocalQuarter> {
		val currentByQuarterId = currentSnapshot.associateBy { quarter -> quarter.id }
		val mergedAffectedById = affectedQuarters
			.map { incomingQuarter ->
				mergeQuarterKeepingLatestSubjects(
					currentQuarter = currentByQuarterId[incomingQuarter.id],
					incomingQuarter = incomingQuarter
				)
			}
			.associateBy { quarter -> quarter.id }
		val earliestAffectedStartDate = mergedAffectedById.values.minOfOrNull { quarter -> quarter.startDate }
			?: return currentSnapshot
		val patchedSnapshot = currentSnapshot
			.map { quarter -> mergedAffectedById[quarter.id] ?: quarter }
			.toMutableList()

		mergedAffectedById.values.forEach { mergedQuarter ->
			if (patchedSnapshot.none { quarter -> quarter.id == mergedQuarter.id }) {
				patchedSnapshot += mergedQuarter
			}
		}

		return recomputeProjectedSnapshot(
			snapshot = patchedSnapshot.toCanonicalOrder(),
			affectedStartDate = earliestAffectedStartDate
		)
	}

	private fun mergeQuarterKeepingLatestSubjects(
		currentQuarter: LocalQuarter?,
		incomingQuarter: LocalQuarter
	): LocalQuarter {
		if (currentQuarter == null) return incomingQuarter

		val incomingSubjectsById = incomingQuarter.subjects.associateBy { subject -> subject.id }
		val mergedSubjects = currentQuarter.subjects
			.map { currentSubject ->
				val incomingSubject = incomingSubjectsById[currentSubject.id]
					?: return@map currentSubject

				if (currentSubject.revision > incomingSubject.revision) {
					currentSubject
				} else {
					incomingSubject
				}
			}
			.toMutableList()

		incomingQuarter.subjects.forEach { incomingSubject ->
			if (mergedSubjects.none { subject -> subject.id == incomingSubject.id }) {
				mergedSubjects += incomingSubject
			}
		}

		return incomingQuarter.copy(
			revision = maxOf(currentQuarter.revision, incomingQuarter.revision),
			subjects = mergedSubjects
		)
	}

	private fun recomputeSubjectGrade(
		snapshot: List<LocalQuarter>,
		qid: String,
		sid: String,
		grade: Int
	): IndexComputationEngine.RecomputeResult {
		val sourceQuarter = snapshot.first { quarter -> quarter.id == qid }
		val updatedQuarter = sourceQuarter.copy(
			subjects = sourceQuarter.subjects.map { subject ->
				if (subject.id == sid) subject.copy(grade = grade) else subject
			}
		)
		val patchedSnapshot = snapshot.map { quarter ->
			if (quarter.id == qid) updatedQuarter else quarter
		}

		return IndexComputationEngine.RecomputeResult(
			quarters = recomputeProjectedSnapshot(
				snapshot = patchedSnapshot,
				affectedStartDate = sourceQuarter.startDate
			),
			affectedQuarters = emptyList()
		)
	}

	private fun recomputeProjectedSnapshot(
		snapshot: List<LocalQuarter>,
		affectedStartDate: Long
	): List<LocalQuarter> {
		val recomputedOfficial = indexComputationEngine.recompute(
			quarters = snapshot,
			affectedStartDate = affectedStartDate
		).quarters.toCanonicalOrder()

		return simulationProjectionEngine.recompute(recomputedOfficial)
			.toCanonicalOrder()
	}

	private fun upsertGradePreview(
		key: SubjectPreviewKey,
		requestedGrade: Int
	) {
		val mutablePreview = gradePreviewSnapshot.toMutableMap()
		val next = SubjectGradePreview(requestedGrade = requestedGrade)
		val previous = mutablePreview.put(key, next)

		if (previous != next) {
			gradePreviewSnapshot = mutablePreview.toMap()
			previewQuartersFlow.value += 1
		}
	}

	private fun removeGradePreview(key: SubjectPreviewKey) {
		removeGradePreviews { candidate, _ -> candidate == key }
	}

	private fun removeGradePreviews(
		predicate: (SubjectPreviewKey, SubjectGradePreview) -> Boolean
	) {
		if (gradePreviewSnapshot.isEmpty()) return

		val filtered = gradePreviewSnapshot
			.filterNot { (key, preview) -> predicate(key, preview) }

		if (filtered.size != gradePreviewSnapshot.size) {
			gradePreviewSnapshot = filtered
			previewQuartersFlow.value += 1
		}
	}

	private fun mergePersistedQuarters(
		current: List<LocalQuarter>,
		updates: List<LocalQuarter>
	): List<LocalQuarter> {
		if (updates.isEmpty()) return current.toCanonicalOrder()

		val updatesById = updates.associateBy { quarter -> quarter.id }
		val merged = current
			.map { quarter -> updatesById[quarter.id] ?: quarter }
			.toMutableList()

		updates.forEach { updatedQuarter ->
			if (merged.none { quarter -> quarter.id == updatedQuarter.id }) {
				merged += updatedQuarter
			}
		}

		return merged.toCanonicalOrder()
	}

	private fun List<LocalQuarter>.toCanonicalOrder(): List<LocalQuarter> {
		return sortedWith(
			compareByDescending<LocalQuarter> { quarter -> quarter.startDate }
				.thenBy { quarter -> quarter.id }
		)
	}
}
