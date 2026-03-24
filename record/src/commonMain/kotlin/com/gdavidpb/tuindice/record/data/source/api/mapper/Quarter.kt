package com.gdavidpb.tuindice.record.data.source.api.mapper

import com.gdavidpb.tuindice.record.data.repository.mutation.RecordMutation
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter
import com.gdavidpb.tuindice.record.data.source.api.response.AddSubjectRequest
import com.gdavidpb.tuindice.record.data.source.api.response.AddQuarterRequest
import com.gdavidpb.tuindice.record.data.source.api.response.QuarterResponse

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
		revision = revision,
		subjects = subjects.map { subjectResponse -> subjectResponse.toRemoteSubject() }
	)
}

fun RecordMutation.AddQuarter.toAddQuarterRequest(
	mutationId: String,
	expectedRevision: Long
): AddQuarterRequest {
	return AddQuarterRequest(
		quarter = quarter,
		year = year,
		subjects = subjects.map { subject ->
			AddSubjectRequest(
				code = subject.code,
				grade = subject.grade
			)
		},
		mutationId = mutationId,
		expectedRevision = expectedRevision
	)
}
