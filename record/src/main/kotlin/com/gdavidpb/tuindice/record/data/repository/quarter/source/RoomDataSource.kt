package com.gdavidpb.tuindice.record.data.repository.quarter.source

import android.util.LruCache
import androidx.room.withTransaction
import com.gdavidpb.tuindice.base.utils.extension.getOrPut
import com.gdavidpb.tuindice.base.utils.extension.selfMapNotNull
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.record.data.repository.quarter.LocalDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalSubject
import com.gdavidpb.tuindice.record.data.repository.quarter.source.database.mapper.toLocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.source.database.mapper.toQuarterEntity
import com.gdavidpb.tuindice.record.data.repository.quarter.source.database.mapper.toSubjectEntity
import com.gdavidpb.tuindice.record.data.utils.computeCredits
import com.gdavidpb.tuindice.record.data.utils.computeGrade
import com.gdavidpb.tuindice.record.data.utils.computeGradeSum
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Objects

class RoomDataSource(
	private val room: TuIndiceDatabase,
	private val quartersCache: HashMap<String, LocalQuarter>,
	private val computationCache: LruCache<Int, LocalQuarter>
) : LocalDataSource {
	override fun getQuartersFlow(): Flow<List<LocalQuarter>> {
		return room.quarters.getQuartersWithSubjectsFlow()
			.map { quarters ->
				quarters
					.map { quarter -> quarter.toLocalQuarter() }
					.also { localQuarters ->
						synchronized(quartersCache) {
							quartersCache.clear()
							quartersCache.putAll(localQuarters.associateBy(LocalQuarter::id))
						}
					}
			}
	}

	override suspend fun getQuarter(qid: String): LocalQuarter? {
		return quartersCache[qid]
	}

	override suspend fun saveQuarters(quarters: List<LocalQuarter>) {
		val quarterEntities = quarters
			.map { quarter -> quarter.toQuarterEntity() }

		val subjectEntities = quarters
			.flatMap { quarter -> quarter.subjects }
			.map { subject -> subject.toSubjectEntity() }

		room.withTransaction {
			room.quarters.upsertEntities(quarterEntities)
			room.subjects.upsertEntities(subjectEntities)
		}
	}

	override suspend fun removeQuarter(qid: String) {
		room.quarters.deleteQuarter(qid = qid)
	}

	override suspend fun saveSubjects(subjects: List<LocalSubject>) {
		val subjectEntities = subjects
			.map { subject -> subject.toSubjectEntity() }

		room.subjects.upsertEntities(subjectEntities)
	}

	override suspend fun computeSetSubjectGrade(qid: String, sid: String, grade: Int): List<LocalQuarter> {
		val subjectQuarter = quartersCache[qid] ?: return emptyList()

		val updatedQuarter = subjectQuarter.copy(
			subjects = subjectQuarter.subjects.map { subject ->
				if (subject.id == sid)
					subject.copy(grade = grade)
				else
					subject
			}
		)

		quartersCache[qid] = updatedQuarter

		val updatedQuarters = synchronized(computationCache) {
			quartersCache
				.values
				.toMutableList()
				.selfMapNotNull { quarter ->
					if (quarter.startDate >= updatedQuarter.startDate) {
						val id = computeIdentifier(
							from = quarter,
							quarters = quartersCache.values
						)

						computationCache
							.getOrPut(id) {
								quarter.copy(
									grade = quarter.subjects.computeGrade(),
									gradeSum = quartersCache.values.computeGradeSum(until = quarter),
									credits = quarter.subjects.computeCredits()
								)
							}
					} else {
						null
					}
				}
		}

		return updatedQuarters
	}

	private fun computeIdentifier(from: LocalQuarter, quarters: Collection<LocalQuarter>): Int {
		val grades = quarters
			.flatMap { quarter ->
				quarter.subjects.map { subject ->
					subject.grade
				}
			}

		return Objects.hash(from.id, grades)
	}
}