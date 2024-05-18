package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source

import androidx.room.withTransaction
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.LocalDataSource
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.model.LocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.database.mapper.toEvaluationEntity
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.database.mapper.toLocalEvaluation
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalSubject
import com.gdavidpb.tuindice.record.data.repository.quarter.source.database.mapper.toLocalQuarter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomDataSource(
	private val room: TuIndiceDatabase
) : LocalDataSource {
	override fun getEvaluationsFlow(uid: String): Flow<List<LocalEvaluation>> {
		return room.evaluations.getEvaluationsWithSubjectFlow(uid)
			.map { evaluations -> evaluations.map { evaluation -> evaluation.toLocalEvaluation() } }
	}

	override suspend fun getEvaluation(uid: String, eid: String): LocalEvaluation? {
		return room.evaluations.getEvaluationWithSubject(uid, eid)
			?.toLocalEvaluation()
	}

	override suspend fun getAvailableSubjects(uid: String): List<LocalSubject> {
		return room.quarters.getCurrentQuarterWithSubjects(uid)
			?.toLocalQuarter()
			?.subjects
			?: emptyList()
	}

	override suspend fun addEvaluation(uid: String, evaluation: LocalEvaluation): LocalEvaluation {
		val evaluationEntity = evaluation.toEvaluationEntity(uid)

		room.evaluations.upsertEntity(entity = evaluationEntity)

		return evaluation
	}

	override suspend fun updateEvaluation(uid: String, evaluation: LocalEvaluation):LocalEvaluation {
		val evaluationEntity = evaluation.toEvaluationEntity(uid)

		room.evaluations.upsertEntity(entity = evaluationEntity)

		return evaluation
	}

	override suspend fun removeEvaluation(uid: String, eid: String) {
		room.evaluations.deleteEvaluation(uid, eid)
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