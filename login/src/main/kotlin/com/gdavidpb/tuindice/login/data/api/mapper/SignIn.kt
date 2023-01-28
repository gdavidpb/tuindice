package com.gdavidpb.tuindice.login.data.api.mapper

import com.gdavidpb.tuindice.login.domain.model.SignIn
import com.gdavidpb.tuindice.login.data.api.response.SignInResponse

fun SignInResponse.toSignIn() = SignIn(
	token = token
)