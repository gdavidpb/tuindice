package com.gdavidpb.tuindice.record.data.repository.quarter.model

data class RemoteAddQuarterAck(
	val mutationId: String,
	val quarter: RemoteQuarter,
	val affectedQuarters: List<RemoteQuarter>
)
