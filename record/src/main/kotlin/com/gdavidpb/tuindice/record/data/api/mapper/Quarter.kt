package com.gdavidpb.tuindice.record.data.api.mapper

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.record.data.api.request.RemoveQuarterRequest
import com.gdavidpb.tuindice.record.data.api.response.QuarterResponse
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import java.util.*

fun QuarterRemove.toRemoveQuarterRequest() = RemoveQuarterRequest(
	quarterId = quarterId
)

fun QuarterResponse.toQuarter() = Quarter(
	id = id,
	name = name,
	startDate = Date(startDate),
	endDate = Date(endDate),
	grade = grade,
	gradeSum = gradeSum,
	credits = credits,
	status = status,
	subjects = subjects.map { subjectResponse -> subjectResponse.toSubject() }.toMutableList()
)