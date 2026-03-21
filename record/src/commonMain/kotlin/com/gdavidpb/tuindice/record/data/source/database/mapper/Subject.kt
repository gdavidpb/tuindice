package com.gdavidpb.tuindice.record.data.source.database.mapper

import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectEntity
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalSubject
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteSubject

fun SubjectEntity.toLocalSubject() = LocalSubject(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	revision = revision
)

fun LocalSubject.toSubjectEntity() = SubjectEntity(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	revision = revision
)

fun RemoteSubject.toLocalSubject() = LocalSubject(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	revision = revision
)

fun LocalSubject.toSubject() = Subject(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade
)

fun Subject.toLocalSubject() = LocalSubject(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	revision = 0L
)
