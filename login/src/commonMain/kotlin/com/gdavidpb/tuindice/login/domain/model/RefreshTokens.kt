package com.gdavidpb.tuindice.login.domain.model

data class RefreshTokens(
	val accessToken: String,
	val refreshToken: String,
	val expiresIn: Long
)