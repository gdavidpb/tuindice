package com.gdavidpb.tuindice.record.data.api.response

import com.google.gson.annotations.SerializedName

data class AddQuarterResponse(
    @SerializedName("quarters") val quarters: List<QuarterResponse>
)