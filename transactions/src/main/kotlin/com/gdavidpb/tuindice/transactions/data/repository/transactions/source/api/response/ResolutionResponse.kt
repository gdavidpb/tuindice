package com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResolutionResponse(
	@SerialName("uid") val uid: String,
	@SerialName("local_reference") val localReference: String,
	@SerialName("remote_reference") val remoteReference: String,
	@SerialName("type") val type: Int,
	@SerialName("action") val action: Int,
	@SerialName("data") val data: String
)