package com.gdavidpb.tuindice.record.data.repository.quarter.source.database

import com.gdavidpb.tuindice.base.domain.model.resolution.Resolution
import com.gdavidpb.tuindice.base.domain.model.resolution.ResolutionAction
import com.gdavidpb.tuindice.base.domain.model.resolution.ResolutionType
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.mapper.toSubjectEntity
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.response.SubjectResponse
import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.database.resolution.ResolutionHandler
import kotlinx.serialization.json.Json

class SubjectResolutionHandler(
	private val room: TuIndiceDatabase
) : ResolutionHandler {
	override suspend fun match(resolution: Resolution): Boolean {
		return (resolution.type == ResolutionType.SUBJECT)
	}

	override suspend fun apply(resolution: Resolution) {
		val subjectEntity = Json.decodeFromString<SubjectResponse>(string = resolution.data)
			.toSubjectEntity(uid = resolution.uid)

		if (resolution.localReference != resolution.remoteReference)
			room.subjects.updateId(
				fromId = resolution.localReference,
				toId = resolution.remoteReference
			)

		when (resolution.action) {
			ResolutionAction.ADD, ResolutionAction.UPDATE ->
				room.subjects.upsertEntities(
					entities = listOf(subjectEntity)
				)

			ResolutionAction.DELETE ->
				room.subjects.deleteSubject(
					uid = subjectEntity.accountId,
					sid = subjectEntity.id
				)
		}
	}
}