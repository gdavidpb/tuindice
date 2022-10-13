package com.gdavidpb.tuindice.record.utils.mappers

import com.gdavidpb.tuindice.base.data.model.database.SubjectUpdate
import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.record.domain.request.UpdateQuarterRequest

fun Quarter.toUpdateRequest(
	sid: String,
	grade: Int,
	dispatchChanges: Boolean,
) = UpdateQuarterRequest(
	qid = id,
	sid = sid,
	update = SubjectUpdate(
		grade = grade
	),
	dispatchChanges = dispatchChanges
)