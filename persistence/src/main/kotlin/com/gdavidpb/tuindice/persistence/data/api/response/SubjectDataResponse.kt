package com.gdavidpb.tuindice.persistence.data.api.response

import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionData
import com.google.gson.annotations.SerializedName

data class SubjectDataResponse(
	@SerializedName("id") val id: String,
	@SerializedName("qid") val quarterId: String,
	@SerializedName("code") val code: String,
	@SerializedName("name") val name: String,
	@SerializedName("credits") val credits: Int,
	@SerializedName("grade") val grade: Int,
	@SerializedName("status") val status: Int,
	@SerializedName("no_effect_by") val noEffectBy: String? = null
) : TransactionData