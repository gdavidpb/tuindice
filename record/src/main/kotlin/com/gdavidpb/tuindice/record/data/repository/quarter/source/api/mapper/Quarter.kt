@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.record.data.repository.quarter.source.api.mapper

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.utils.DEFAULT_TIME_ZONE
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.response.AddQuarterRequest
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.response.QuarterResponse
import kotlinx.datetime.Month
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

fun QuarterResponse.toRemoteQuarter(): RemoteQuarter {
	return RemoteQuarter(
		id = id,
		name = name,
		startDate = startDate,
		endDate = endDate,
		grade = grade,
		gradeSum = gradeSum,
		credits = credits,
		creditsSum = creditsSum,
		isCurrent = isCurrent,
		isReadOnly = isReadOnly,
		subjects = subjects.map { subjectResponse -> subjectResponse.toRemoteSubject() }
	)
}

fun RemoteQuarter.toAddQuarterRequest(): AddQuarterRequest {
	val localTime = Instant.fromEpochMilliseconds(startDate)
		.toLocalDateTime(DEFAULT_TIME_ZONE)

	val year = localTime.year
	val quarter = when (localTime.month) {
		Month.JANUARY -> 1
		Month.APRIL -> 2
		Month.JUNE -> 3
		Month.SEPTEMBER -> 4
		else -> throw IllegalArgumentException()
	}

	return AddQuarterRequest(
		quarter = quarter,
		year = year,
		subjects = subjects.map { remoteSubject -> remoteSubject.toAddSubjectRequest() }
	)
}

fun Quarter.toRemoteQuarter() = RemoteQuarter(
	id = id,
	name = name,
	startDate = startDate,
	endDate = endDate,
	grade = grade,
	gradeSum = gradeSum,
	credits = credits,
	creditsSum = creditsSum,
	isCurrent = isCurrent,
	isReadOnly = isReadOnly,
	subjects = subjects.map { subject -> subject.toRemoteSubject() }
)