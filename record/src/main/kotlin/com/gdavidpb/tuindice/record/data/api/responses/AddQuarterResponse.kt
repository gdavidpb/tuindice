package com.gdavidpb.tuindice.record.data.api.responses

import com.google.gson.annotations.SerializedName

data class AddQuarterResponse(
    @SerializedName("quarters") val quarters: List<QuarterResponse>
)