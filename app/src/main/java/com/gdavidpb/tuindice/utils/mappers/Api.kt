package com.gdavidpb.tuindice.utils.mappers

import com.gdavidpb.tuindice.data.source.functions.responses.SignInResponse
import com.gdavidpb.tuindice.domain.model.SignIn

fun SignInResponse.toSignIn() = SignIn(
	token = token
)