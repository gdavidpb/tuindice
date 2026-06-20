package com.gdavidpb.tuindice.auth.domain.usecase.param

import com.gdavidpb.tuindice.auth.domain.model.SignInIdentifierMode

data class SignInParams(
	val usbId: String,
	val password: String,
	val identifierMode: SignInIdentifierMode = SignInIdentifierMode.UsbId
)
