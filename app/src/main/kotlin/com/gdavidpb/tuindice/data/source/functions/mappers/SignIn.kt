package com.gdavidpb.tuindice.data.source.functions.mappers

import com.gdavidpb.tuindice.base.domain.model.SignIn
import com.gdavidpb.tuindice.data.source.functions.responses.SignInResponse

fun SignInResponse.toSignIn() = SignIn(
	token = token
)