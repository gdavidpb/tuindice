package com.gdavidpb.tuindice.evaluations.data.room

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.data.evaluation.source.LocalDataSource
import com.gdavidpb.tuindice.evaluations.data.room.mapper.toEvaluation
import com.gdavidpb.tuindice.evaluations.data.room.mapper.toEvaluationEntity
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomDataSource(
	private val room: TuIndiceDatabase
) : LocalDataSource {
	override suspend fun getEvaluation(uid: String, eid: String): Flow<Evaluation?> {
		return room.evaluations.getEvaluation(uid, eid)
			.map { evaluation -> evaluation?.toEvaluation() }
	}

	override suspend fun getEvaluations(uid: String, sid: String): Flow<List<Evaluation>> {
		return room.evaluations.getSubjectEvaluations(uid, sid)
			.map { evaluations -> evaluations.map { evaluation -> evaluation.toEvaluation() } }
	}

	override suspend fun saveEvaluations(uid: String, evaluations: List<Evaluation>) {
		val evaluationEntities = evaluations
			.map { evaluation -> evaluation.toEvaluationEntity(uid) }

		room.evaluations.upsertEntities(evaluationEntities)
	}

	override suspend fun removeEvaluation(uid: String, eid: String) {
		room.evaluations.deleteEvaluation(uid, eid)
	}
}