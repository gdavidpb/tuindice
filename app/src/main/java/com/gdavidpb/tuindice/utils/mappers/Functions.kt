package com.gdavidpb.tuindice.utils.mappers

import com.gdavidpb.tuindice.data.source.functions.responses.SignInResponse
import com.gdavidpb.tuindice.domain.model.functions.SignInResult

fun SignInResponse.toSignInResult() = SignInResult(
	token = token
)