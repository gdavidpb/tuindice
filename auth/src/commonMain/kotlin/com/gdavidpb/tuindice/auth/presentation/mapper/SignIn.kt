package com.gdavidpb.tuindice.auth.presentation.mapper

import com.gdavidpb.tuindice.auth.domain.usecase.param.SignInParams
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn

fun (SignIn.Action.ClickSignIn).toSignInParams() = SignInParams(
	usbId = usbId,
	password = password
)