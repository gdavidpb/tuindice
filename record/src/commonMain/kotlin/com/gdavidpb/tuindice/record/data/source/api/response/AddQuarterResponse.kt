package com.gdavidpb.tuindice.record.data.source.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddQuarterResponse(
	@SerialName("mutation_id") val mutationId: String,
	@SerialName("quarter_patch") val quarterPatch: QuarterResponse,
	@SerialName("affected_quarters") val affectedQuarters: List<QuarterResponse>
)
