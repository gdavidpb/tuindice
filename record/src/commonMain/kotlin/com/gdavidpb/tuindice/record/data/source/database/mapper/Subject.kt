package com.gdavidpb.tuindice.record.data.source.database.mapper

import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectEntity
import com.gdavidpb.tuindice.record.data.model.quarter.LocalSubject
import com.gdavidpb.tuindice.record.data.model.quarter.RemoteSubject

fun SubjectEntity.toLocalSubject() = LocalSubject(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	status = status.toSubjectStatus(),
	revision = revision
)

fun LocalSubject.toSubjectEntity() = SubjectEntity(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	status = status?.value,
	revision = revision
)

fun RemoteSubject.toLocalSubject() = LocalSubject(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	status = status,
	revision = revision
)

fun LocalSubject.toSubject() = Subject(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	status = status
)

fun Subject.toLocalSubject() = LocalSubject(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	status = status,
	revision = 0L
)

private fun String?.toSubjectStatus(): SubjectStatus? {
	return SubjectStatus.entries.firstOrNull { status -> status.value == this }
}
