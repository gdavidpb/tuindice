package com.gdavidpb.tuindice.record.data.repository.quarter.source.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddQuarterResponse(
	@SerialName("quarters") val quarters: List<QuarterResponse>
)