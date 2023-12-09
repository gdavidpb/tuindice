package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source

import androidx.room.withTransaction
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.LocalDataSource
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.database.mapper.toEvaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.database.mapper.toEvaluationEntity
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.database.mapper.toTransactionEntity
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.mapper.toQuarter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomDataSource(
	private val room: TuIndiceDatabase
) : LocalDataSource {
	override suspend fun getEvaluations(uid: String): Flow<List<Evaluation>> {
		return room.evaluations.getEvaluationsWithSubject(uid)
			.map { evaluations -> evaluations.map { evaluation -> evaluation.toEvaluation() } }
	}

	override suspend fun getEvaluation(uid: String, eid: String): Evaluation? {
		return room.evaluations.getEvaluationWithSubject(uid, eid)
			?.toEvaluation()
	}

	override suspend fun getAvailableSubjects(uid: String): List<Subject> {
		return room.quarters.getCurrentQuarterWithSubjects(uid)
			?.toQuarter()
			?.subjects
			?: emptyList()
	}

	override suspend fun addEvaluation(uid: String, add: EvaluationAdd) {
		val evaluationEntity = add.toEvaluationEntity(uid)
		val transactionEntity = add.toTransactionEntity(uid)

		room.withTransaction {
			room.evaluations.upsertEntity(entity = evaluationEntity)
			room.transactions.enqueueTransaction(entity = transactionEntity)
		}
	}

	override suspend fun updateEvaluation(uid: String, update: EvaluationUpdate) {
		val evaluationEntity = room.evaluations
			.getEvaluation(
				uid = uid,
				eid = update.evaluationId
			)

		val updatedEvaluationEntity = evaluationEntity.copy(
			grade = update.grade,
			maxGrade = update.maxGrade ?: evaluationEntity.maxGrade,
			date = update.date ?: evaluationEntity.date,
			type = update.type ?: evaluationEntity.type
		)

		val transactionEntity = update.toTransactionEntity(uid)

		room.withTransaction {
			room.evaluations.upsertEntity(entity = updatedEvaluationEntity)
			room.transactions.enqueueTransaction(entity = transactionEntity)
		}
	}

	override suspend fun removeEvaluation(uid: String, remove: EvaluationRemove) {
		val transactionEntity = remove.toTransactionEntity(uid)

		room.withTransaction {
			room.evaluations.deleteEvaluation(
				uid = uid,
				eid = remove.evaluationId
			)

			room.transactions.dequeueTransactionsByReference(
				uid = uid,
				reference = remove.evaluationId
			)

			room.transactions.enqueueTransaction(
				entity = transactionEntity
			)
		}
	}

	override suspend fun saveEvaluations(uid: String, evaluations: List<Evaluation>) {
		room.withTransaction {
			evaluations.forEach { evaluation ->
				val evaluationEntity = evaluation.toEvaluationEntity(uid)

				room.evaluations.upsertEntity(
					entity = evaluationEntity
				)

				room.transactions.dequeueTransactionsByReference(
					uid = uid,
					reference = evaluation.evaluationId
				)
			}
		}
	}
}