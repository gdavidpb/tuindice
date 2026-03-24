package com.gdavidpb.tuindice.record.data.repository.quarter.model

import com.gdavidpb.tuindice.record.data.repository.mutation.RecordMutationAck

data class RemoteSetSubjectGradeAck(
	override val mutationId: String,
	val subject: RemoteSubject,
	val affectedQuarters: List<RemoteQuarter>
) : RecordMutationAck
