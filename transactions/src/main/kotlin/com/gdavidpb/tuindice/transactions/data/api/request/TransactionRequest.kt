package com.gdavidpb.tuindice.transactions.data.api.request

import com.gdavidpb.tuindice.transactions.data.api.model.transaction.TransactionRequestAction
import com.gdavidpb.tuindice.transactions.data.api.model.transaction.TransactionRequestType
import com.google.gson.annotations.SerializedName

data class TransactionRequest(
	@SerializedName("reference") val reference: String,
	@SerializedName("type") val type: TransactionRequestType,
	@SerializedName("action") val action: TransactionRequestAction,
	@SerializedName("data") val data: String
)