package com.gdavidpb.tuindice.auth.domain.usecase.param

data class SignInParams(
	val usbId: String,
	val password: String
)