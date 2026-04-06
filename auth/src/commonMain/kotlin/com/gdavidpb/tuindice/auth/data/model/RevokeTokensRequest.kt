package com.gdavidpb.tuindice.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RevokeTokensRequest(
	@SerialName("session_id")
	val sessionId: String,
	@SerialName("refresh_token")
	val refreshToken: String
)
