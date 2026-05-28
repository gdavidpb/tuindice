package com.gdavidpb.tuindice.data.source.messaging.api.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscribeRequest(
	@SerialName("token") val token: String
)
