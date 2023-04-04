package com.gdavidpb.tuindice.record.data.api.mapper

import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectUpdateTransaction
import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.record.data.api.request.UpdateSubjectRequest
import com.gdavidpb.tuindice.record.data.api.response.SubjectResponse

fun SubjectResponse.toSubject() = Subject(
	id = id,
	qid = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	status = status
)

fun Transaction<SubjectUpdateTransaction>.toUpdateSubjectRequest() = UpdateSubjectRequest(
	subjectId = data.subjectId,
	grade = data.grade
)