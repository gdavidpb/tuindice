package com.gdavidpb.tuindice.record.data.model.quarter

import com.gdavidpb.tuindice.record.data.mutation.RecordMutationAck

data class RemoteSetSubjectGradeAck(
	override val mutationId: String,
	val subject: RemoteSubject,
	val affectedQuarters: List<RemoteQuarter>
) : RecordMutationAck
