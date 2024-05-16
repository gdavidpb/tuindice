package com.gdavidpb.tuindice.record.data.repository.quarter.source.api.mapper

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_CURRENT
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_MOCK
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_RETIRED
import com.gdavidpb.tuindice.persistence.data.room.entity.QuarterEntity
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.request.UpdateQuarterRequest
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.response.QuarterResponse
import com.gdavidpb.tuindice.record.domain.model.QuarterUpdate

fun QuarterResponse.toRemoteQuarter(): RemoteQuarter {
	val isEditable = (status == STATUS_QUARTER_CURRENT) || (status == STATUS_QUARTER_MOCK)
	val isRetired = (status == STATUS_QUARTER_RETIRED)

	return RemoteQuarter(
		id = id,
		name = name,
		startDate = startDate,
		endDate = endDate,
		grade = grade,
		gradeSum = gradeSum,
		credits = credits,
		status = status,
		isEditable = isEditable,
		isRetired = isRetired,
		subjects = subjects.map { subjectResponse -> subjectResponse.toRemoteSubject(isEditable) }
	)
}

fun QuarterResponse.toQuarter(): Quarter {
	val isEditable = (status == STATUS_QUARTER_CURRENT) || (status == STATUS_QUARTER_MOCK)
	val isRetired = (status == STATUS_QUARTER_RETIRED)

	return Quarter(
		id = id,
		name = name,
		startDate = startDate,
		endDate = endDate,
		grade = grade,
		gradeSum = gradeSum,
		credits = credits,
		status = status,
		isEditable = isEditable,
		isRetired = isRetired,
		subjects = subjects.map { subjectResponse -> subjectResponse.toSubject(isEditable) }
	)
}

fun QuarterUpdate.toUpdateQuarterRequest() = UpdateQuarterRequest(
	quarterId = id,
	subjectsUpdates = subjectsUpdates.map { subjectUpdate -> subjectUpdate.toUpdateSubjectRequest() }
)

fun QuarterResponse.toQuarterAndSubjectsEntities(uid: String) = QuarterEntity(
	id = id,
	accountId = uid,
	name = name,
	status = status,
	startDate = startDate,
	endDate = endDate,
	grade = grade,
	gradeSum = gradeSum,
	credits = credits
) to subjects.map { subjectResponse -> subjectResponse.toSubjectEntity(uid) }