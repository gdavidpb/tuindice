package com.gdavidpb.tuindice.login.presentation.mapper

import com.gdavidpb.tuindice.login.domain.usecase.param.SignInParams
import com.gdavidpb.tuindice.login.presentation.contract.SignIn

fun (SignIn.Action.ClickSignIn).toSignInParams() = SignInParams(
	usbId = usbId,
	password = password
)