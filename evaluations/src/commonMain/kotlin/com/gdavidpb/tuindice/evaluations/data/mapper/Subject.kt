package com.gdavidpb.tuindice.evaluations.data.mapper

import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.evaluations.data.model.LocalSubject
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectEntity

fun SubjectEntity.toLocalSubject() = LocalSubject(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade
)

fun LocalSubject.toSubject() = Subject(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade
)
