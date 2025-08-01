package com.gdavidpb.tuindice.evaluations.data.source

import androidx.room.withTransaction
import com.gdavidpb.tuindice.evaluations.data.mapper.toEvaluationEntity
import com.gdavidpb.tuindice.evaluations.data.mapper.toLocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.repository.DatabaseDataSource
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalSubject
import com.gdavidpb.tuindice.record.data.repository.quarter.source.database.mapper.toLocalSubject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomDatabaseDataSource(
	private val room: TuIndiceDatabase
) : DatabaseDataSource {
	override fun getEvaluationsFlow(): Flow<List<LocalEvaluation>> {
		return room.evaluations.getEvaluationsWithSubjectFlow()
			.map { evaluations -> evaluations.map { evaluation -> evaluation.toLocalEvaluation() } }
	}

	override suspend fun getEvaluation(eid: String): LocalEvaluation? {
		return room.evaluations.getEvaluationWithSubject(eid)
			?.toLocalEvaluation()
	}

	override suspend fun getAvailableSubjects(): List<LocalSubject> {
		return room.quarters.getOpenQuartersWithSubjects()
			.flatMap { quarter -> quarter.subjects }
			.map { subject -> subject.toLocalSubject(isEditable = true) }
	}

	override suspend fun addEvaluation(evaluation: LocalEvaluation): LocalEvaluation {
		val evaluationEntity = evaluation.toEvaluationEntity()

		room.evaluations.upsertEntity(entity = evaluationEntity)

		return evaluation
	}

	override suspend fun updateEvaluation(evaluation: LocalEvaluation): LocalEvaluation {
		val evaluationEntity = evaluation.toEvaluationEntity()

		room.evaluations.upsertEntity(entity = evaluationEntity)

		return evaluation
	}

	override suspend fun removeEvaluation(eid: String) {
		room.evaluations.deleteEvaluation(eid)
	}

	override suspend fun saveEvaluations(evaluations: List<LocalEvaluation>) {
		room.withTransaction {
			evaluations.forEach { evaluation ->
				val evaluationEntity = evaluation.toEvaluationEntity()

				room.evaluations.upsertEntity(
					entity = evaluationEntity
				)
			}
		}
	}
}