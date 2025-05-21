package com.gdavidpb.tuindice.record.data.repository.quarter.source.api.mapper

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.utils.DEFAULT_TIME_ZONE
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_CURRENT
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_MOCK
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_RETIRED
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.response.AddQuarterRequest
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.response.QuarterResponse
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime

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

fun RemoteQuarter.toAddQuarterRequest(): AddQuarterRequest {
	val localTime = Instant.fromEpochMilliseconds(startDate)
		.toLocalDateTime(DEFAULT_TIME_ZONE)

	val year = localTime.year
	val quarter = when (localTime.monthNumber) {
		1 -> 1
		4 -> 2
		6 -> 3
		9 -> 4
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
	status = status,
	isEditable = isEditable,
	isRetired = isRetired,
	subjects = subjects.map { subject -> subject.toRemoteSubject() }
)