package com.gdavidpb.tuindice.record.data.repository.quarter.model

import com.gdavidpb.tuindice.record.data.repository.mutation.RecordMutationAck

data class RemoteAddQuarterAck(
	override val mutationId: String,
	val quarter: RemoteQuarter,
	val affectedQuarters: List<RemoteQuarter>
) : RecordMutationAck
