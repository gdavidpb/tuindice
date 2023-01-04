package com.gdavidpb.tuindice.login.domain.model

data class SignInRequest(
	val usbId: String,
	val password: String
)