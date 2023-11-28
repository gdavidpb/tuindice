package com.gdavidpb.tuindice.record.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddQuarterResponse(
	@SerialName("quarters") val quarters: List<QuarterResponse>
)