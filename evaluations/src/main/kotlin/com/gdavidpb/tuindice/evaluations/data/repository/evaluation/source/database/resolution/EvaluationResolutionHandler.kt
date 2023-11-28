package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.database.resolution

import com.gdavidpb.tuindice.base.domain.model.resolution.Resolution
import com.gdavidpb.tuindice.base.domain.model.resolution.ResolutionAction
import com.gdavidpb.tuindice.base.domain.model.resolution.ResolutionType
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.mapper.toEvaluationEntity
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.response.EvaluationResponse
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.database.resolution.ResolutionHandler
import kotlinx.serialization.json.Json

class EvaluationResolutionHandler(
	private val room: TuIndiceDatabase
) : ResolutionHandler {
	override suspend fun match(resolution: Resolution): Boolean {
		return (resolution.type == ResolutionType.EVALUATION)
	}

	override suspend fun apply(resolution: Resolution) {
		val subjectEntity = Json.decodeFromString<EvaluationResponse>(string = resolution.data)
			.toEvaluationEntity(uid = resolution.uid)

		if (resolution.localReference != resolution.remoteReference)
			room.subjects.updateId(
				fromId = resolution.localReference,
				toId = resolution.remoteReference
			)

		when (resolution.action) {
			ResolutionAction.ADD, ResolutionAction.UPDATE ->
				room.evaluations.upsertEntities(
					entities = listOf(subjectEntity)
				)

			ResolutionAction.DELETE ->
				room.evaluations.deleteEvaluation(
					uid = subjectEntity.accountId,
					eid = subjectEntity.id
				)
		}
	}
}