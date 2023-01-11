package com.gdavidpb.tuindice.evaluations.data.room

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.data.evaluation.source.LocalDataSource
import com.gdavidpb.tuindice.evaluations.data.room.mappers.toEvaluation
import com.gdavidpb.tuindice.evaluations.data.room.mappers.toEvaluationEntity
import com.gdavidpb.tuindice.persistence.data.source.room.TuIndiceDatabase

class RoomDataSource(
	private val room: TuIndiceDatabase
) : LocalDataSource {
	override suspend fun getEvaluation(uid: String, eid: String): Evaluation {
		val evaluationEntity = room.evaluations.getEvaluation(uid, eid)

		return evaluationEntity.toEvaluation()
	}

	override suspend fun getEvaluations(uid: String, sid: String): List<Evaluation> {
		val evaluationsEntities = room.evaluations.getSubjectEvaluations(uid, sid)

		return evaluationsEntities.map { evaluationEntity ->
			evaluationEntity.toEvaluation()
		}
	}

	override suspend fun saveEvaluations(uid: String, vararg evaluations: Evaluation) {
		val evaluationEntities = evaluations
			.map { evaluation -> evaluation.toEvaluationEntity(uid) }
			.toTypedArray()

		room.evaluations.insert(*evaluationEntities)
	}

	override suspend fun removeEvaluation(uid: String, eid: String) {
		room.evaluations.deleteEvaluation(uid, eid)
	}
}