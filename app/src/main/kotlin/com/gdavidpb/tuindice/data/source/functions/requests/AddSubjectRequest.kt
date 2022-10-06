package com.gdavidpb.tuindice.data.source.functions.requests

import com.google.gson.annotations.SerializedName

data class AddSubjectRequest(
    @SerializedName("code") val code: String,
    @SerializedName("grade") val grade: Int
)