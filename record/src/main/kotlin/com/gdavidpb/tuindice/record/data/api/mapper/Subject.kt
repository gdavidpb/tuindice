package com.gdavidpb.tuindice.record.data.api.mapper

import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectEntity
import com.gdavidpb.tuindice.record.data.api.request.UpdateSubjectRequest
import com.gdavidpb.tuindice.record.data.api.response.SubjectResponse
import com.gdavidpb.tuindice.record.domain.model.SubjectUpdate

fun SubjectUpdate.toUpdateSubjectRequest() = UpdateSubjectRequest(
	subjectId = subjectId,
	grade = grade
)

fun SubjectResponse.toSubject() = Subject(
	id = id,
	qid = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	status = status
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