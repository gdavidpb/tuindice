package com.gdavidpb.tuindice.record.data.api.mapper

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.record.data.api.response.QuarterResponse
import java.util.*

fun QuarterResponse.toQuarter() = Quarter(
	id = id,
	name = name,
	startDate = Date(startDate),
	endDate = Date(endDate),
	grade = grade,
	gradeSum = gradeSum,
	credits = credits,
	status = status,
	subjects = subjects.map { subjectResponse ->
		subjectResponse.toSubject()
	}.toMutableList()
)