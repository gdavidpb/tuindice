package com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionRequest(
	@SerialName("reference") val reference: String,
	@SerialName("type") val type: Int,
	@SerialName("action") val action: Int,
	@SerialName("data") val data: String? = null
)