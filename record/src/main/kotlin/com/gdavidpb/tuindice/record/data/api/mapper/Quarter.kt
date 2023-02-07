package com.gdavidpb.tuindice.record.data.api.mapper

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.record.data.api.response.GetQuartersResponse
import com.gdavidpb.tuindice.record.data.api.response.QuarterResponse
import com.gdavidpb.tuindice.record.domain.model.Quarters
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

fun GetQuartersResponse.toGetQuarters() = Quarters(
	quarters = quarters.map { quarterResponse -> quarterResponse.toQuarter() },
	lastUpdate = lastUpdate
)