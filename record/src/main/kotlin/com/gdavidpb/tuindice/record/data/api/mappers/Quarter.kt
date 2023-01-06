package com.gdavidpb.tuindice.record.data.api.mappers

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.record.data.api.responses.QuarterResponse
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
		subjectResponse.toSubject(qid = id)
	}.toMutableList()
)