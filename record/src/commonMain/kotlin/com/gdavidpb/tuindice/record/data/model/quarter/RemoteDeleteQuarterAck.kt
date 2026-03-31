package com.gdavidpb.tuindice.record.data.model.quarter

import com.gdavidpb.tuindice.record.data.mutation.RecordMutationAck

data class RemoteDeleteQuarterAck(
	override val mutationId: String,
	val removedQuarterId: String,
	val affectedQuarters: List<RemoteQuarter>
) : RecordMutationAck
