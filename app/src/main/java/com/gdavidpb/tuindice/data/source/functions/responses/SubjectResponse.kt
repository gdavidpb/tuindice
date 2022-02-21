package com.gdavidpb.tuindice.data.source.functions.responses

import com.google.gson.annotations.SerializedName

data class SubjectResponse(
    @SerializedName("id") val id: String,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("credits") val credits: Int,
    @SerializedName("grade") val grade: Int,
    @SerializedName("status") val status: Int,
    @SerializedName("no_effect_by") val noEffectBy: String? = null
)