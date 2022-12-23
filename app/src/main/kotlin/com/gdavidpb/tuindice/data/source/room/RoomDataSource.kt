package com.gdavidpb.tuindice.data.source.room

import com.gdavidpb.tuindice.base.data.model.database.EvaluationUpdate
import com.gdavidpb.tuindice.base.data.model.database.QuarterUpdate
import com.gdavidpb.tuindice.base.data.model.database.SubjectUpdate
import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.base.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.base.utils.extensions.tomorrow
import com.gdavidpb.tuindice.base.utils.extensions.withTransaction
import com.gdavidpb.tuindice.data.source.room.mappers.*
import java.util.*

class RoomDataSource(
	private val room: TuIndiceDatabase
) : DatabaseRepository {
	override suspend fun isUpdated(uid: String): Boolean {
		val now = Date()
		val lastUpdate = room.accounts.getLastUpdate(uid)
		val outdated = lastUpdate.tomorrow()

		return now.before(outdated)
	}

	override suspend fun addAccount(uid: String, account: Account) {
		val accountEntity = account.toAccountEntity(uid)

		room.accounts.insert(accountEntity)
	}

	override suspend fun getAccount(uid: String): Account {
		val accountEntity = room.accounts.getAccount(uid)

		return accountEntity.toAccount()
	}

	override suspend fun addQuarter(uid: String, quarter: Quarter) {
		val subjectsEntities = quarter.subjects
			.map { subject -> subject.toSubjectEntity(uid) }
			.toTypedArray()

		val quarterEntity = quarter.toQuarterEntity(uid)

		room.withTransaction {
			subjects.insert(*subjectsEntities)
			quarters.insert(quarterEntity)
		}
	}

	override suspend fun getQuarter(uid: String, qid: String): Quarter {
		val (quarterEntity, subjectsEntities) = room.quarters.getQuarter(uid, qid)

		return quarterEntity.toQuarter(subjectsEntities)
	}

	override suspend fun getQuarters(uid: String): List<Quarter> {
		val quartersWithSubjects = room.quarters.getQuarters(uid)

		return quartersWithSubjects.map { (quarterEntity, subjectsEntities) ->
			quarterEntity.toQuarter(subjectsEntities)
		}
	}

	override suspend fun updateQuarter(uid: String, qid: String, update: QuarterUpdate): Quarter {
		TODO("Not yet implemented")
	}

	override suspend fun removeQuarter(uid: String, qid: String) {
		TODO("Not yet implemented")
	}

	override suspend fun getSubject(uid: String, sid: String): Subject {
		TODO("Not yet implemented")
	}

	override suspend fun getQuarterSubjects(uid: String, qid: String): List<Subject> {
		TODO("Not yet implemented")
	}

	override suspend fun updateSubject(uid: String, sid: String, update: SubjectUpdate): Subject {
		TODO("Not yet implemented")
	}

	override suspend fun addEvaluation(uid: String, evaluation: Evaluation): Evaluation {
		TODO("Not yet implemented")
	}

	override suspend fun getEvaluation(uid: String, eid: String): Evaluation {
		TODO("Not yet implemented")
	}

	override suspend fun getSubjectEvaluations(uid: String, sid: String): List<Evaluation> {
		TODO("Not yet implemented")
	}

	override suspend fun updateEvaluation(
		uid: String,
		eid: String,
		update: EvaluationUpdate
	): Evaluation {
		TODO("Not yet implemented")
	}

	override suspend fun removeEvaluation(uid: String, eid: String) {
		TODO("Not yet implemented")
	}

	override suspend fun close() {
		TODO("Not yet implemented")
	}
}