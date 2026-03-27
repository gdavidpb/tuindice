package com.gdavidpb.tuindice.record.data.source.api.mapper

import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteSubject
import com.gdavidpb.tuindice.record.data.source.api.response.SubjectResponse

fun SubjectResponse.toRemoteSubject() = RemoteSubject(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	status = status,
	revision = revision
)
