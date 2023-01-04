package com.gdavidpb.tuindice.login.data.api.mappers

import com.gdavidpb.tuindice.login.domain.model.SignIn
import com.gdavidpb.tuindice.login.data.api.responses.SignInResponse

fun SignInResponse.toSignIn() = SignIn(
	token = token
)