package com.gdavidpb.tuindice.record.data.repository.quarter.source.api.mapper

import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.utils.STATUS_SUBJECT_NO_EFFECT
import com.gdavidpb.tuindice.base.utils.STATUS_SUBJECT_RETIRED
import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteSubject
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.request.UpdateSubjectRequest
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.response.SubjectResponse

fun SubjectResponse.toRemoteSubject(isEditable: Boolean) = RemoteSubject(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	status = status,
	isEditable = isEditable,
	isRetired = (!isEditable && status == STATUS_SUBJECT_RETIRED) || (isEditable && grade == MIN_SUBJECT_GRADE),
	isNoEffect = (status == STATUS_SUBJECT_NO_EFFECT)
)

fun RemoteSubject.toUpdateSubjectRequest() = UpdateSubjectRequest(
	subjectId = id,
	grade = grade
)

fun Subject.toRemoteSubject() = RemoteSubject(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	status = status,
	isEditable = isEditable,
	isRetired = isRetired,
	isNoEffect = isNoEffect
)