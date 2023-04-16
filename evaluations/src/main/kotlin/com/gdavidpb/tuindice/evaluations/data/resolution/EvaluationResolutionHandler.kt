package com.gdavidpb.tuindice.evaluations.data.resolution

import com.gdavidpb.tuindice.base.domain.model.resolution.Resolution
import com.gdavidpb.tuindice.base.domain.model.resolution.ResolutionAction
import com.gdavidpb.tuindice.base.domain.model.resolution.ResolutionType
import com.gdavidpb.tuindice.base.utils.extension.fromJson
import com.gdavidpb.tuindice.evaluations.data.api.mapper.toEvaluationEntity
import com.gdavidpb.tuindice.evaluations.data.api.response.EvaluationResponse
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.transactions.data.room.resolution.ResolutionHandler
import com.google.gson.Gson

class EvaluationResolutionHandler(
	private val gson: Gson,
	private val room: TuIndiceDatabase
) : ResolutionHandler {
	override suspend fun match(resolution: Resolution): Boolean {
		return (resolution.type == ResolutionType.EVALUATION)
	}

	override suspend fun apply(resolution: Resolution) {
		val subjectEntity = gson.fromJson<EvaluationResponse>(json = resolution.data)
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