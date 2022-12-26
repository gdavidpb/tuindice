package com.gdavidpb.tuindice.data.source.functions.mappers

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.data.source.functions.responses.QuarterResponse
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