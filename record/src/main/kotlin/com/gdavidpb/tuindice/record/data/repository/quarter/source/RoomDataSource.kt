package com.gdavidpb.tuindice.record.data.repository.quarter.source

import androidx.room.withTransaction
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.record.data.repository.quarter.LocalDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalSubject
import com.gdavidpb.tuindice.record.data.repository.quarter.source.database.mapper.toLocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.source.database.mapper.toQuarterEntity
import com.gdavidpb.tuindice.record.data.repository.quarter.source.database.mapper.toSubjectEntity
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.QuarterUpdate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomDataSource(
	private val room: TuIndiceDatabase
) : LocalDataSource {
	override fun getQuartersFlow(uid: String): Flow<List<LocalQuarter>> {
		return room.quarters.getQuartersWithSubjectsFlow(uid)
			.map { quarters -> quarters.map { quarter -> quarter.toLocalQuarter() } }
	}

	override suspend fun getQuarters(uid: String): List<LocalQuarter> {
		return room.quarters.getQuartersWithSubjects(uid)
			.map { quarter -> quarter.toLocalQuarter() }
	}

	override suspend fun getQuarter(uid: String, qid: String): LocalQuarter? {
		return room.quarters.getQuarterWithSubjects(uid, qid)?.toLocalQuarter()
	}

	override suspend fun saveQuarters(uid: String, quarters: List<LocalQuarter>) {
		val quarterEntities = quarters
			.map { quarter -> quarter.toQuarterEntity(uid) }

		val subjectEntities = quarters
			.flatMap { quarter -> quarter.subjects }
			.map { subject -> subject.toSubjectEntity(uid) }

		room.withTransaction {
			room.quarters.upsertEntities(quarterEntities)
			room.subjects.upsertEntities(subjectEntities)
		}
	}

	override suspend fun removeQuarter(uid: String, remove: QuarterRemove) {
		room.quarters.deleteQuarter(uid = uid, qid = remove.id)
	}

	override suspend fun updateQuarter(uid: String, update: QuarterUpdate) {
		room.withTransaction {
			update.subjectsUpdates.forEach { subjectUpdate ->
				room.subjects.updateSubject(
					uid = uid,
					sid = subjectUpdate.id,
					grade = subjectUpdate.grade
				)
			}
		}
	}

	override suspend fun saveSubjects(uid: String, subjects: List<LocalSubject>) {
		val subjectEntities = subjects
			.map { subject -> subject.toSubjectEntity(uid) }

		room.subjects.upsertEntities(subjectEntities)
	}
}