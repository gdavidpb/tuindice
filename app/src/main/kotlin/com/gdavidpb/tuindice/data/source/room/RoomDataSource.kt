package com.gdavidpb.tuindice.data.source.room

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

	override suspend fun removeQuarter(uid: String, qid: String) {
		room.quarters.deleteQuarter(uid, qid)
	}

	override suspend fun getSubject(uid: String, sid: String): Subject {
		val subjectEntity = room.subjects.getSubject(uid, sid)

		return subjectEntity.toSubject()
	}

	override suspend fun addEvaluation(uid: String, evaluation: Evaluation) {
		val evaluationEntity = evaluation.toEvaluationEntity(uid)

		room.evaluations.insert(evaluationEntity)
	}

	override suspend fun getEvaluation(uid: String, eid: String): Evaluation {
		val evaluationEntity = room.evaluations.getEvaluation(uid, eid)

		return evaluationEntity.toEvaluation()
	}

	override suspend fun getSubjectEvaluations(uid: String, sid: String): List<Evaluation> {
		val evaluationsEntities = room.evaluations.getSubjectEvaluations(uid, sid)

		return evaluationsEntities.map { evaluationEntity ->
			evaluationEntity.toEvaluation()
		}
	}

	override suspend fun removeEvaluation(uid: String, eid: String) {
		room.evaluations.deleteEvaluation(uid, eid)
	}

	override suspend fun close() {
		room.close()
	}
}