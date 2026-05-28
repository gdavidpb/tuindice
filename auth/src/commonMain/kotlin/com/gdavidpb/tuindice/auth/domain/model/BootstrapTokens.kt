package com.gdavidpb.tuindice.auth.domain.model

data class BootstrapTokens(
	val uid: String,
	val usbId: String,
	val accessToken: String,
	val expiresIn: Long
)
