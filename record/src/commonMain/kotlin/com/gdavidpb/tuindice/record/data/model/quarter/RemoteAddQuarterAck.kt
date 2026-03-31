package com.gdavidpb.tuindice.record.data.model.quarter

import com.gdavidpb.tuindice.record.data.mutation.RecordMutationAck

data class RemoteAddQuarterAck(
	override val mutationId: String,
	val quarter: RemoteQuarter,
	val affectedQuarters: List<RemoteQuarter>
) : RecordMutationAck
