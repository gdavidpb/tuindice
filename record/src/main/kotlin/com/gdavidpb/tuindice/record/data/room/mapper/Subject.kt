package com.gdavidpb.tuindice.record.data.room.mapper

import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectEntity

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