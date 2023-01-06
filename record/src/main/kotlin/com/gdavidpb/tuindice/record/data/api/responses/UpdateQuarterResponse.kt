package com.gdavidpb.tuindice.record.data.api.responses

import com.google.gson.annotations.SerializedName

data class UpdateQuarterResponse(
	@SerializedName("quarters") val quarters: List<QuarterResponse>
)