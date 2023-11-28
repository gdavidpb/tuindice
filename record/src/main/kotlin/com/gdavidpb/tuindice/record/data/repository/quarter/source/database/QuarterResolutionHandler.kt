package com.gdavidpb.tuindice.record.data.repository.quarter.source.database

import com.gdavidpb.tuindice.base.domain.model.resolution.Resolution
import com.gdavidpb.tuindice.base.domain.model.resolution.ResolutionAction
import com.gdavidpb.tuindice.base.domain.model.resolution.ResolutionType
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.mapper.toQuarterAndSubjectsEntities
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.response.QuarterResponse
import com.gdavidpb.tuindice.transactions.data.room.resolution.ResolutionHandler
import kotlinx.serialization.json.Json

class QuarterResolutionHandler(
	private val room: TuIndiceDatabase
) : ResolutionHandler {
	override suspend fun match(resolution: Resolution): Boolean {
		return (resolution.type == ResolutionType.QUARTER)
	}

	override suspend fun apply(resolution: Resolution) {
		val (quarterEntity, subjectEntities) = Json.decodeFromString<QuarterResponse>(string = resolution.data)
			.toQuarterAndSubjectsEntities(uid = resolution.uid)

		if (resolution.localReference != resolution.remoteReference)
			room.quarters.updateId(
				fromId = resolution.localReference,
				toId = resolution.remoteReference
			)

		when (resolution.action) {
			ResolutionAction.ADD, ResolutionAction.UPDATE -> {
				room.quarters.upsertEntities(
					entities = listOf(quarterEntity)
				)

				room.subjects.upsertEntities(
					entities = subjectEntities
				)
			}

			ResolutionAction.DELETE ->
				room.quarters.deleteQuarter(
					uid = quarterEntity.accountId,
					qid = quarterEntity.id
				)
		}
	}
}