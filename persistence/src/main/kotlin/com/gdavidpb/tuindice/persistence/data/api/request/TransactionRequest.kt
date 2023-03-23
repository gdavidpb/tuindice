package com.gdavidpb.tuindice.persistence.data.api.request

import com.gdavidpb.tuindice.persistence.data.api.model.TransactionRequestAction
import com.gdavidpb.tuindice.persistence.data.api.model.TransactionRequestType
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionData
import com.google.gson.annotations.SerializedName

data class TransactionRequest(
	@SerializedName("reference") val reference: String,
	@SerializedName("type") val type: TransactionRequestType,
	@SerializedName("action") val action: TransactionRequestAction,
	@SerializedName("timestamp") val timestamp: Long,
	@SerializedName("data") val data: TransactionData? = null
)