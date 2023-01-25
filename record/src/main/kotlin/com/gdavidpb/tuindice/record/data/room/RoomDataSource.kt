package com.gdavidpb.tuindice.record.data.room

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.persistence.data.source.room.TuIndiceDatabase
import com.gdavidpb.tuindice.record.data.room.mapper.toQuarter
import com.gdavidpb.tuindice.record.data.room.mapper.toQuarterEntity
import com.gdavidpb.tuindice.record.data.room.mapper.toSubjectEntity
import com.gdavidpb.tuindice.persistence.data.source.room.utils.extensions.withTransaction
import com.gdavidpb.tuindice.record.data.quarter.source.LocalDataSource

class RoomDataSource(
	private val room: TuIndiceDatabase
) : LocalDataSource {
	override suspend fun isUpdated(uid: String): Boolean {
		return room.accounts.isUpdated(uid)
	}

	override suspend fun getQuarters(uid: String): List<Quarter> {
		return room.quarters.getQuarters(uid)
			.map { (quarter, subjects) -> quarter.toQuarter(subjects) }
	}

	override suspend fun saveQuarters(uid: String, quarters: List<Quarter>) {
		val quarterEntities = quarters
			.map { quarter -> quarter.toQuarterEntity(uid) }
			.toTypedArray()

		val subjectEntities = quarters
			.flatMap { quarter -> quarter.subjects }
			.map { subject -> subject.toSubjectEntity(uid) }
			.toTypedArray()

		room.withTransaction {
			this.quarters.insert(*quarterEntities)
			this.subjects.insert(*subjectEntities)
		}
	}

	override suspend fun removeQuarter(uid: String, qid: String) {
		room.quarters.deleteQuarter(uid, qid)
	}

	override suspend fun saveSubjects(uid: String, vararg subjects: Subject) {
		val subjectEntities = subjects
			.map { subject -> subject.toSubjectEntity(uid) }
			.toTypedArray()

		room.subjects.insert(*subjectEntities)
	}
}