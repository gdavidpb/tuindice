package com.gdavidpb.tuindice.login.data.repository.login.source.api.mapper

import com.gdavidpb.tuindice.login.domain.model.SignIn
import com.gdavidpb.tuindice.login.data.repository.login.source.api.response.SignInResponse

fun SignInResponse.toSignIn() = SignIn(
	token = token
)