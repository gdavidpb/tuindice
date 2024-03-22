package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source

import androidx.room.withTransaction
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.LocalDataSource
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.model.LocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.database.mapper.toEvaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.database.mapper.toEvaluationEntity
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.mapper.toQuarter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomDataSource(
	private val room: TuIndiceDatabase
) : LocalDataSource {
	override fun getEvaluations(uid: String): Flow<List<LocalEvaluation>> {
		return room.evaluations.getEvaluationsWithSubject(uid)
			.map { evaluations -> evaluations.map { evaluation -> evaluation.toEvaluation() } }
	}

	override suspend fun getEvaluation(uid: String, eid: String): LocalEvaluation? {
		return room.evaluations.getEvaluationWithSubject(uid, eid)
			?.toEvaluation()
	}

	override suspend fun getAvailableSubjects(uid: String): List<Subject> {
		return room.quarters.getCurrentQuarterWithSubjects(uid)
			?.toQuarter()
			?.subjects
			?: emptyList()
	}

	override suspend fun addEvaluation(uid: String, evaluation: Evaluation) {
		val evaluationEntity = evaluation.toEvaluationEntity(uid)

		room.evaluations.upsertEntity(entity = evaluationEntity)
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

		room.evaluations.upsertEntity(entity = updatedEvaluationEntity)
	}

	override suspend fun removeEvaluation(uid: String, remove: EvaluationRemove) {
		room.evaluations.deleteEvaluation(
			uid = uid,
			eid = remove.evaluationId
		)
	}

	override suspend fun saveEvaluations(uid: String, evaluations: List<LocalEvaluation>) {
		room.withTransaction {
			evaluations.forEach { evaluation ->
				val evaluationEntity = evaluation.toEvaluationEntity(uid)

				room.evaluations.upsertEntity(
					entity = evaluationEntity
				)
			}
		}
	}
}