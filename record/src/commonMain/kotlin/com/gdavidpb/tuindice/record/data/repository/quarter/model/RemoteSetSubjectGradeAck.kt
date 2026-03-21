package com.gdavidpb.tuindice.record.data.repository.quarter.model

data class RemoteSetSubjectGradeAck(
	val mutationId: String,
	val subject: RemoteSubject,
	val affectedQuarters: List<RemoteQuarter>
)
