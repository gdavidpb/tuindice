package com.gdavidpb.tuindice.record.data.source.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeleteQuarterResponse(
	@SerialName("mutation_id") val mutationId: String,
	@SerialName("removed_quarter_id") val removedQuarterId: String,
	@SerialName("affected_quarters") val affectedQuarters: List<QuarterResponse>
)

