package com.gdavidpb.tuindice.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokensResponse(
	@SerialName("session_id") val sessionId: String,
	@SerialName("access_token") val accessToken: String,
	@SerialName("refresh_token") val refreshToken: String,
	@SerialName("expires_in") val expiresIn: Long
)
