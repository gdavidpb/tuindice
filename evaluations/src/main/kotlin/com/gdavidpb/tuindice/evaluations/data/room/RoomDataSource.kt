package com.gdavidpb.tuindice.evaluations.data.room

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.evaluations.data.evaluation.source.LocalDataSource
import com.gdavidpb.tuindice.evaluations.data.room.mapper.toEvaluation
import com.gdavidpb.tuindice.evaluations.data.room.mapper.toEvaluationEntity
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
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

	override suspend fun getEvaluation(uid: String, eid: String): Flow<Evaluation?> {
		return room.evaluations.getEvaluationWithSubject(uid, eid)
			.map { evaluation -> evaluation?.toEvaluation() }
	}

	override suspend fun addEvaluation(uid: String, add: EvaluationAdd) {
		val evaluationEntity = add.toEvaluationEntity(uid = uid)

		room.evaluations.upsertEntities(listOf(evaluationEntity))
	}

	override suspend fun saveEvaluations(uid: String, evaluations: List<Evaluation>) {
		val evaluationEntities = evaluations
			.map { evaluation -> evaluation.toEvaluationEntity(uid) }

		room.evaluations.upsertEntities(evaluationEntities)
	}

	override suspend fun removeEvaluation(uid: String, remove: EvaluationRemove) {
		room.evaluations.deleteEvaluation(uid = uid, eid = remove.evaluationId)
	}

	override suspend fun getAvailableSubjects(uid: String): Flow<List<Subject>> {
		return room.quarters.getCurrentQuarterWithSubjects(uid)
			.map { quarter -> quarter?.toQuarter()?.subjects ?: listOf() }
	}
}