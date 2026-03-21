package com.gdavidpb.tuindice.record.data.repository.quarter.model

data class RemoteDeleteQuarterAck(
	val mutationId: String,
	val removedQuarterId: String,
	val affectedQuarters: List<RemoteQuarter>
)

