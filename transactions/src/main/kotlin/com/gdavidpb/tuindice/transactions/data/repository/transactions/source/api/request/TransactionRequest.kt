package com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.request

import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.model.TransactionRequestAction
import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.model.TransactionRequestType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionRequest(
	@SerialName("reference") val reference: String,
	@SerialName("type") val type: TransactionRequestType,
	@SerialName("action") val action: TransactionRequestAction,
	@SerialName("data") val data: String? = null
)