package com.gdavidpb.tuindice.record.data.repository.quarter.source

import androidx.room.withTransaction
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.record.data.repository.quarter.LocalDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalSubject
import com.gdavidpb.tuindice.record.data.repository.quarter.model.SetSubjectGradeResult
import com.gdavidpb.tuindice.record.data.repository.quarter.source.database.mapper.toLocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.source.database.mapper.toQuarterEntity
import com.gdavidpb.tuindice.record.data.repository.quarter.source.database.mapper.toSubjectEntity
import com.gdavidpb.tuindice.record.data.utils.IndexComputationEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private data class SubjectPreviewKey(
	val quarterId: String,
	val subjectId: String
)

class RoomDataSource(
	private val room: TuIndiceDatabase,
	private val indexComputationEngine: IndexComputationEngine
) : LocalDataSource {
	private val writeMutex = Mutex()
	private val previewQuartersFlow = MutableStateFlow(0L)

	@Volatile
	private var inMemoryQuartersSnapshot: List<LocalQuarter>? = null

	@Volatile
	private var previewOverridesSnapshot: Map<SubjectPreviewKey, Int> = emptyMap()

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

		return combine(quartersFlow, previewQuartersFlow) { confirmedQuarters, _ ->
			applyPreviewToSnapshot(confirmedQuarters)
		}
	}

	override suspend fun getQuarter(qid: String): LocalQuarter? {
		val confirmedSnapshot = inMemoryQuartersSnapshot ?: loadSnapshotFromRoom()
		val previewSnapshot = applyPreviewToSnapshot(confirmedSnapshot)

		return previewSnapshot.firstOrNull { quarter -> quarter.id == qid }
	}

	override suspend fun saveQuarters(quarters: List<LocalQuarter>) {
		val quarterEntities = quarters
			.map { quarter -> quarter.toQuarterEntity() }

		val subjectEntities = quarters
			.flatMap { quarter -> quarter.subjects }
			.map { subject -> subject.toSubjectEntity() }

		writeMutex.withLock {
			room.withTransaction {
				room.quarters.upsertEntities(quarterEntities)
				room.subjects.upsertEntities(subjectEntities)
			}

			inMemoryQuartersSnapshot = quarters.toCanonicalOrder()
			clearPreviewOverrides()
		}
	}

	override suspend fun removeQuarter(qid: String) {
		writeMutex.withLock {
			room.quarters.deleteQuarter(qid = qid)

			inMemoryQuartersSnapshot = inMemoryQuartersSnapshot
				?.filterNot { quarter -> quarter.id == qid }

			removePreviewOverrides { key -> key.quarterId == qid }
		}
	}

	override suspend fun saveSubjects(subjects: List<LocalSubject>) {
		val subjectEntities = subjects
			.map { subject -> subject.toSubjectEntity() }
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

			removePreviewOverrides { key -> key.subjectId in updatedSubjectIds }
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
				?: return@withLock SetSubjectGradeResult(
					updatedQuarters = emptyList(),
					updatedTargetQuarter = null
				)
			val sourceSubject = sourceQuarter.subjects
				.firstOrNull { subject -> subject.id == sid }
				?: return@withLock SetSubjectGradeResult(
					updatedQuarters = emptyList(),
					updatedTargetQuarter = null
				)
			val key = SubjectPreviewKey(
				quarterId = qid,
				subjectId = sid
			)
			val affectedStartDate = sourceQuarter.startDate

			if (!commit) {
				upsertPreviewOverride(
					key = key,
					confirmedGrade = sourceSubject.grade,
					requestedGrade = grade
				)

				val previewSnapshot = applyPreviewToSnapshot(confirmedSnapshot)

				return@withLock SetSubjectGradeResult(
					updatedQuarters = previewSnapshot.filter { quarter ->
						quarter.startDate >= affectedStartDate
					},
					updatedTargetQuarter = previewSnapshot
						.firstOrNull { quarter -> quarter.id == qid }
				)
			}

			val hadPreviewOverride = previewOverridesSnapshot.containsKey(key)

			if (grade == sourceSubject.grade) {
				if (hadPreviewOverride) removePreviewOverride(key)

				return@withLock SetSubjectGradeResult(
					updatedQuarters = emptyList(),
					updatedTargetQuarter = null
				)
			}

			val quarterToUpdate = sourceQuarter.copy(
				subjects = sourceQuarter.subjects.map { subject ->
					if (subject.id == sid) subject.copy(grade = grade) else subject
				}
			)

			val patchedSnapshot = confirmedSnapshot.map { quarter ->
				if (quarter.id == qid) quarterToUpdate else quarter
			}

			val recomputed = indexComputationEngine.recompute(
				quarters = patchedSnapshot,
				affectedStartDate = affectedStartDate
			)

			room.withTransaction {
				room.subjects.updateSubject(
					sid = sid,
					grade = grade
				)
				room.quarters.upsertEntities(
					recomputed.affectedQuarters
						.map { quarter -> quarter.toQuarterEntity() }
				)
			}

			inMemoryQuartersSnapshot = recomputed.quarters.toCanonicalOrder()

			if (hadPreviewOverride) removePreviewOverride(key)

			SetSubjectGradeResult(
				updatedQuarters = recomputed.affectedQuarters,
				updatedTargetQuarter = recomputed.quarters
					.firstOrNull { quarter -> quarter.id == qid }
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

	private fun applyPreviewToSnapshot(confirmedSnapshot: List<LocalQuarter>): List<LocalQuarter> {
		val overrides = previewOverridesSnapshot

		if (overrides.isEmpty()) return confirmedSnapshot

		var affectedStartDate = Long.MAX_VALUE
		var hasChanges = false

		val patchedSnapshot = confirmedSnapshot.map { quarter ->
			var quarterChanged = false

			val patchedSubjects = quarter.subjects.map { subject ->
				val previewGrade = overrides[SubjectPreviewKey(quarter.id, subject.id)]
					?: return@map subject

				if (previewGrade == subject.grade) return@map subject

				hasChanges = true
				quarterChanged = true
				affectedStartDate = minOf(affectedStartDate, quarter.startDate)

				subject.copy(grade = previewGrade)
			}

			if (quarterChanged)
				quarter.copy(subjects = patchedSubjects)
			else
				quarter
		}

		if (!hasChanges) return confirmedSnapshot

		return indexComputationEngine.recompute(
			quarters = patchedSnapshot,
			affectedStartDate = affectedStartDate
		).quarters.toCanonicalOrder()
	}

	private fun upsertPreviewOverride(
		key: SubjectPreviewKey,
		confirmedGrade: Int,
		requestedGrade: Int
	) {
		val mutableOverrides = previewOverridesSnapshot.toMutableMap()
		val changed = if (requestedGrade == confirmedGrade)
			mutableOverrides.remove(key) != null
		else {
			val previous = mutableOverrides.put(key, requestedGrade)
			previous != requestedGrade
		}

		if (changed) {
			previewOverridesSnapshot = mutableOverrides.toMap()
			previewQuartersFlow.value += 1
		}
	}

	private fun clearPreviewOverrides() {
		if (previewOverridesSnapshot.isEmpty()) return

		previewOverridesSnapshot = emptyMap()
		previewQuartersFlow.value += 1
	}

	private fun removePreviewOverride(key: SubjectPreviewKey) {
		removePreviewOverrides { candidate -> candidate == key }
	}

	private fun removePreviewOverrides(predicate: (SubjectPreviewKey) -> Boolean) {
		if (previewOverridesSnapshot.isEmpty()) return

		val filtered = previewOverridesSnapshot.filterKeys { key -> !predicate(key) }

		if (filtered.size != previewOverridesSnapshot.size) {
			previewOverridesSnapshot = filtered
			previewQuartersFlow.value += 1
		}
	}

	private fun List<LocalQuarter>.toCanonicalOrder(): List<LocalQuarter> {
		return sortedWith(
			compareByDescending<LocalQuarter> { quarter -> quarter.startDate }
				.thenBy { quarter -> quarter.id }
		)
	}
}