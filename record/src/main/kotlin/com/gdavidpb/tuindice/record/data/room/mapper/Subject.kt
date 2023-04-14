package com.gdavidpb.tuindice.record.data.room.mapper

import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectEntity
import com.gdavidpb.tuindice.record.data.api.response.SubjectResponse

fun Subject.toSubjectEntity(uid: String) = SubjectEntity(
	id = id,
	quarterId = qid,
	accountId = uid,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	status = status
)

fun SubjectEntity.toSubject() = Subject(
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