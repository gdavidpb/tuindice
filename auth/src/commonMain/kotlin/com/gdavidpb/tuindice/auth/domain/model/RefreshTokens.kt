package com.gdavidpb.tuindice.auth.domain.model

data class RefreshTokens(
	val sessionId: String,
	val accessToken: String,
	val refreshToken: String,
	val expiresIn: Long
)
