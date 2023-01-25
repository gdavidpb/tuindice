package com.gdavidpb.tuindice.record.data.api.mapper

import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.record.data.api.request.UpdateSubjectRequest
import com.gdavidpb.tuindice.record.data.api.response.SubjectResponse
import com.gdavidpb.tuindice.record.domain.model.UpdateSubject

fun SubjectResponse.toSubject() = Subject(
	id = id,
	qid = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	status = status
)

fun UpdateSubject.toSubjectEvaluationRequest() = UpdateSubjectRequest(
	subjectId = subjectId,
	grade = grade
)