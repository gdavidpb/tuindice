package com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.response

import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.model.ResolutionResponseAction
import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.model.ResolutionResponseType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResolutionResponse(
	@SerialName("uid") val uid: String,
	@SerialName("local_reference") val localReference: String,
	@SerialName("remote_reference") val remoteReference: String,
	@SerialName("type") val type: ResolutionResponseType,
	@SerialName("action") val action: ResolutionResponseAction,
	@SerialName("data") val data: String
)