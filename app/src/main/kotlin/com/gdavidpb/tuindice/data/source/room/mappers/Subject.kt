package com.gdavidpb.tuindice.data.source.room.mappers

import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.data.source.room.entities.SubjectEntity

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