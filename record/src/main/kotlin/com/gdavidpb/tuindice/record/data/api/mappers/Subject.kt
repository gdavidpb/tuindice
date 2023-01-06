package com.gdavidpb.tuindice.record.data.api.mappers

import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.record.data.api.responses.SubjectResponse

fun SubjectResponse.toSubject(qid: String) = Subject(
	id = id,
	qid = qid,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	status = status
)