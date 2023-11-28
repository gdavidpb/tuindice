package com.gdavidpb.tuindice.record.data.repository.quarter.source.api.mapper

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_CURRENT
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_MOCK
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_RETIRED
import com.gdavidpb.tuindice.persistence.data.room.entity.QuarterEntity
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.response.QuarterResponse

fun QuarterResponse.toQuarter(): Quarter {
	val isEditable = (status == STATUS_QUARTER_CURRENT) || (status == STATUS_QUARTER_MOCK)

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
		isRetired = (status == STATUS_QUARTER_RETIRED),
		subjects = subjects.map { subjectResponse -> subjectResponse.toSubject(isEditable) }
	)
}

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