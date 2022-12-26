package com.gdavidpb.tuindice.data.source.functions.mappers

import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.data.source.functions.responses.SubjectResponse

fun SubjectResponse.toSubject(qid: String) = Subject(
	id = id,
	qid = qid,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	status = status
)