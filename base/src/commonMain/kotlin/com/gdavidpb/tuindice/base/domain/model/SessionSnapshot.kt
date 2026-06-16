package com.gdavidpb.tuindice.base.domain.model

data class SessionSnapshot(
	val sessionId: String,
	val accessToken: String,
	val refreshToken: String,
	val usbId: String
)
