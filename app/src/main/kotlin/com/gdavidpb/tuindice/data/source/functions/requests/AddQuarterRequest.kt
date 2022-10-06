package com.gdavidpb.tuindice.data.source.functions.requests

import com.google.gson.annotations.SerializedName

data class AddQuarterRequest(
    @SerializedName("quarter") val quarter: Int,
    @SerializedName("year") val year: Int,
    @SerializedName("subjects") val subjects: List<AddSubjectRequest>
)