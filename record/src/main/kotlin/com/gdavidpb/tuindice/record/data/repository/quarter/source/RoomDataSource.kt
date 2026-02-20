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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RoomDataSource(
	private val room: TuIndiceDatabase,
	private val indexComputationEngine: IndexComputationEngine
) : LocalDataSource {
	private val writeMutex = Mutex()

	override fun getQuartersFlow(): Flow<List<LocalQuarter>> {
		return room.quarters.getQuartersWithSubjectsFlow()
			.map { quarters ->
				quarters
					.map { quarter -> quarter.toLocalQuarter() }
			}
	}

	override suspend fun getQuarter(qid: String): LocalQuarter? {
		return room.quarters
			.getQuarterWithSubjects(qid = qid)
			?.toLocalQuarter()
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
		}
	}

	override suspend fun removeQuarter(qid: String) {
		writeMutex.withLock {
			room.quarters.deleteQuarter(qid = qid)
		}
	}

	override suspend fun saveSubjects(subjects: List<LocalSubject>) {
		val subjectEntities = subjects
			.map { subject -> subject.toSubjectEntity() }

		writeMutex.withLock {
			room.subjects.upsertEntities(subjectEntities)
		}
	}

	override suspend fun setSubjectGradeAndRecompute(
		qid: String,
		sid: String,
		grade: Int
	): SetSubjectGradeResult {
		return writeMutex.withLock {
			room.withTransaction {
				val snapshot = room.quarters
					.getQuartersWithSubjects()
					.map { quarter -> quarter.toLocalQuarter() }

				val sourceQuarter = snapshot
					.firstOrNull { quarter -> quarter.id == qid }
					?: return@withTransaction SetSubjectGradeResult(
						updatedQuarters = emptyList(),
						updatedTargetQuarter = null
					)

				if (sourceQuarter.subjects.none { subject -> subject.id == sid }) {
					return@withTransaction SetSubjectGradeResult(
						updatedQuarters = emptyList(),
						updatedTargetQuarter = null
					)
				}

				val quarterToUpdate = sourceQuarter.copy(
					subjects = sourceQuarter.subjects.map { subject ->
						if (subject.id == sid)
							subject.copy(grade = grade)
						else
							subject
					}
				)

				val patchedSnapshot = snapshot.map { quarter ->
					if (quarter.id == qid)
						quarterToUpdate
					else
						quarter
				}

				val recomputed = indexComputationEngine.recompute(
					quarters = patchedSnapshot,
					affectedStartDate = sourceQuarter.startDate
				)

				room.subjects.updateSubject(
					sid = sid,
					grade = grade
				)
				room.quarters.upsertEntities(
					recomputed.affectedQuarters
						.map { quarter -> quarter.toQuarterEntity() }
				)

				SetSubjectGradeResult(
					updatedQuarters = recomputed.affectedQuarters,
					updatedTargetQuarter = recomputed.quarters
						.firstOrNull { quarter -> quarter.id == qid }
				)
			}
		}
	}
}
