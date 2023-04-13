package com.gdavidpb.tuindice.record.data.api.request

import com.google.gson.annotations.SerializedName

data class RemoveQuarterRequest(
	@SerializedName("quarter_id") val quarterId: String
)
