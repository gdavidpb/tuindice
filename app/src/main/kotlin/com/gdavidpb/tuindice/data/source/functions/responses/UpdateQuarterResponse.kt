package com.gdavidpb.tuindice.data.source.functions.responses

import com.google.gson.annotations.SerializedName

data class UpdateQuarterResponse(
	@SerializedName("quarters") val quarters: List<QuarterResponse>
)