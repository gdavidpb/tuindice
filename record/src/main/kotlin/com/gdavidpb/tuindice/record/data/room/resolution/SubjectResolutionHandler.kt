package com.gdavidpb.tuindice.record.data.room.resolution

import com.gdavidpb.tuindice.base.domain.model.resolution.Resolution
import com.gdavidpb.tuindice.base.domain.model.resolution.ResolutionAction
import com.gdavidpb.tuindice.base.domain.model.resolution.ResolutionType
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.transactions.data.room.resolution.ResolutionHandler
import com.gdavidpb.tuindice.transactions.domain.mapper.toSubjectEntity

class SubjectResolutionHandler(
	private val room: TuIndiceDatabase
) : ResolutionHandler {
	override suspend fun match(resolution: Resolution): Boolean {
		return (resolution.type == ResolutionType.SUBJECT)
	}

	override suspend fun apply(resolution: Resolution) {
		val subjectEntity = resolution.toSubjectEntity()

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