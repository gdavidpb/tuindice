package com.gdavidpb.tuindice.persistence.data.api.response

import com.gdavidpb.tuindice.persistence.data.api.model.TransactionRequestAction
import com.gdavidpb.tuindice.persistence.data.api.model.TransactionRequestType
import com.gdavidpb.tuindice.persistence.domain.model.TransactionData
import com.google.gson.annotations.SerializedName

data class ResolutionResponse(
	@SerializedName("reference") val reference: String,
	@SerializedName("type") val type: TransactionRequestType,
	@SerializedName("action") val action: TransactionRequestAction,
	@SerializedName("data") val data: TransactionData? = null
)