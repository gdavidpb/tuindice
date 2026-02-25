package com.gdavidpb.tuindice.login.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscribeRequest(
	@SerialName("token") val token: String
)