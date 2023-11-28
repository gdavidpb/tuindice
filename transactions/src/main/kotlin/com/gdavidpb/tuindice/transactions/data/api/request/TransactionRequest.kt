package com.gdavidpb.tuindice.transactions.data.api.request

import com.gdavidpb.tuindice.transactions.data.api.model.transaction.TransactionRequestAction
import com.gdavidpb.tuindice.transactions.data.api.model.transaction.TransactionRequestType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionRequest(
	@SerialName("reference") val reference: String,
	@SerialName("type") val type: TransactionRequestType,
	@SerialName("action") val action: TransactionRequestAction,
	@SerialName("data") val data: String
)