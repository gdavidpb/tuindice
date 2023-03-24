package com.gdavidpb.tuindice.persistence.data.api.mapper

import com.gdavidpb.tuindice.base.domain.model.subject.SubjectUpdateResolution
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectUpdateTransaction
import com.gdavidpb.tuindice.persistence.data.api.request.subject.SubjectUpdateRequest
import com.gdavidpb.tuindice.persistence.data.api.response.subject.SubjectUpdateResponse

fun SubjectUpdateTransaction.toSubjectUpdateRequest() = SubjectUpdateRequest(
	id = subjectId,
	grade = grade
)

fun SubjectUpdateResponse.toSubjectUpdateResolution() = SubjectUpdateResolution(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	status = status
)