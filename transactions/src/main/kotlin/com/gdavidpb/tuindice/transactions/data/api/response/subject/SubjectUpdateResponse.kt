package com.gdavidpb.tuindice.transactions.data.api.response.subject

import com.gdavidpb.tuindice.transactions.data.api.response.ResolutionOperationResponse
import com.google.gson.annotations.SerializedName

data class SubjectUpdateResponse(
	@SerializedName("id") val id: String,
	@SerializedName("qid") val quarterId: String,
	@SerializedName("code") val code: String,
	@SerializedName("name") val name: String,
	@SerializedName("credits") val credits: Int,
	@SerializedName("grade") val grade: Int,
	@SerializedName("status") val status: Int,
	@SerializedName("no_effect_by") val noEffectBy: String? = null
) : ResolutionOperationResponse