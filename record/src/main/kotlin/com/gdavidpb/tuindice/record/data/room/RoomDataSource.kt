package com.gdavidpb.tuindice.record.data.room

import androidx.room.withTransaction
import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.quarter.QuarterRemoveTransaction
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectUpdateTransaction
import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.record.data.quarter.source.LocalDataSource
import com.gdavidpb.tuindice.record.data.room.mapper.toQuarter
import com.gdavidpb.tuindice.record.data.room.mapper.toQuarterEntity
import com.gdavidpb.tuindice.record.data.room.mapper.toSubject
import com.gdavidpb.tuindice.record.data.room.mapper.toSubjectEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomDataSource(
	private val room: TuIndiceDatabase
) : LocalDataSource {
	override suspend fun getQuarters(uid: String): Flow<List<Quarter>> {
		return room.quarters.getQuartersWithSubjects(uid)
			.map { quarters -> quarters.map { quarter -> quarter.toQuarter() } }
	}

	override suspend fun saveQuarters(uid: String, quarters: List<Quarter>) {
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

	override suspend fun removeQuarter(uid: String, transaction: Transaction<QuarterRemoveTransaction>) {
		room.quarters.deleteQuarter(uid = uid, qid = transaction.data.quarterId)
	}

	override suspend fun saveSubjects(uid: String, subjects: List<Subject>) {
		val subjectEntities = subjects
			.map { subject -> subject.toSubjectEntity(uid) }

		room.subjects.upsertEntities(subjectEntities)
	}

	override suspend fun updateSubject(uid: String, transaction: Transaction<SubjectUpdateTransaction>): Subject {
		room.subjects.updateSubject(
			uid = uid,
			sid = transaction.data.subjectId,
			grade = transaction.data.grade
		)

		val subjectEntity = room.subjects.getSubject(
			uid = uid,
			sid = transaction.data.subjectId
		)

		return subjectEntity.toSubject()
	}
}