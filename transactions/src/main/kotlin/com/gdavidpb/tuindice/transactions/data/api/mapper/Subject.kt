package com.gdavidpb.tuindice.transactions.data.api.mapper

import com.gdavidpb.tuindice.base.domain.model.subject.SubjectUpdateResolution
import com.gdavidpb.tuindice.transactions.data.api.response.subject.SubjectUpdateResponse

fun SubjectUpdateResponse.toSubjectUpdateResolution() = SubjectUpdateResolution(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	status = status
)