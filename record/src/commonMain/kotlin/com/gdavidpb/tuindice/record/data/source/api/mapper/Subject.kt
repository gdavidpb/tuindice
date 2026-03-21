package com.gdavidpb.tuindice.record.data.source.api.mapper

import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteSubject
import com.gdavidpb.tuindice.record.data.source.api.response.AddSubjectRequest
import com.gdavidpb.tuindice.record.data.source.api.response.SubjectResponse

fun SubjectResponse.toRemoteSubject() = RemoteSubject(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	revision = revision
)

fun RemoteSubject.toAddSubjectRequest() = AddSubjectRequest(
	code = code,
	grade = grade
)

fun Subject.toRemoteSubject() = RemoteSubject(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	revision = 0L
)
