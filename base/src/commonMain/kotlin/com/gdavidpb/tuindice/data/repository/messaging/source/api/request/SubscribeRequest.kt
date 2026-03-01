package com.gdavidpb.tuindice.data.repository.messaging.source.api.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscribeRequest(
	@SerialName("token") val token: String
)
