package com.gdavidpb.tuindice.record.data.repository.quarter.model

import com.gdavidpb.tuindice.record.data.repository.mutation.RecordMutationAck

data class RemoteDeleteQuarterAck(
	override val mutationId: String,
	val removedQuarterId: String,
	val affectedQuarters: List<RemoteQuarter>
) : RecordMutationAck
