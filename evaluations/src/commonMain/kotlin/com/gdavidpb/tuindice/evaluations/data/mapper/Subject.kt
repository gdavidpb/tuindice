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
	grade = grade,
	gradingMode = com.gdavidpb.tuindice.base.domain.model.subject.GradingMode.entries.firstOrNull { mode ->
		mode.value == gradingMode
	} ?: com.gdavidpb.tuindice.base.domain.model.subject.GradingMode.NUMERIC
)

fun LocalSubject.toSubject() = Subject(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	gradingMode = gradingMode
)
