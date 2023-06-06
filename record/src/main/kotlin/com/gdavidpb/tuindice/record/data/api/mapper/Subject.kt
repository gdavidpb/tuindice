package com.gdavidpb.tuindice.record.data.api.mapper

import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.utils.STATUS_SUBJECT_NO_EFFECT
import com.gdavidpb.tuindice.base.utils.STATUS_SUBJECT_RETIRED
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectEntity
import com.gdavidpb.tuindice.record.data.api.request.UpdateSubjectRequest
import com.gdavidpb.tuindice.record.data.api.response.SubjectResponse
import com.gdavidpb.tuindice.record.domain.model.SubjectUpdate
import com.gdavidpb.tuindice.record.utils.MIN_SUBJECT_GRADE

fun SubjectUpdate.toUpdateSubjectRequest() = UpdateSubjectRequest(
	subjectId = subjectId,
	grade = grade
)

fun SubjectResponse.toSubject(isEditable: Boolean) = Subject(
	id = id,
	qid = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	status = status,
	isEditable = isEditable,
	isRetired = (!isEditable && status == STATUS_SUBJECT_RETIRED) || (isEditable && grade == MIN_SUBJECT_GRADE),
	isNoEffect = (status == STATUS_SUBJECT_NO_EFFECT)
)

fun SubjectResponse.toSubjectEntity(uid: String) = SubjectEntity(
	id = id,
	quarterId = quarterId,
	accountId = uid,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	status = status
)