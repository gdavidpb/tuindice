package com.gdavidpb.tuindice.record.data.api.response

import com.google.gson.annotations.SerializedName

data class GetQuartersResponse(
	@SerializedName("quarters") val quarters: List<QuarterResponse>,
	@SerializedName("last_update") val lastUpdate: Long
)