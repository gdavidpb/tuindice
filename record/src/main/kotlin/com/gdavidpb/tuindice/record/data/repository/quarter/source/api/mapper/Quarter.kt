package com.gdavidpb.tuindice.record.data.repository.quarter.source.api.mapper

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_CURRENT
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_MOCK
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_RETIRED
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.request.UpdateQuarterRequest
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.response.QuarterResponse

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

fun RemoteQuarter.toUpdateQuarterRequest() = UpdateQuarterRequest(
	quarterId = id,
	subjectsUpdates = subjects.map { subject -> subject.toUpdateSubjectRequest() }
)

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