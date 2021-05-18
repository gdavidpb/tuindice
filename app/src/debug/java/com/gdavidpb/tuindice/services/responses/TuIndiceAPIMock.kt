package com.gdavidpb.tuindice.services.responses

import com.gdavidpb.tuindice.data.source.functions.responses.SignInResponse
import java.util.*

val defaultSignInResponse = SignInResponse(
	token = UUID.randomUUID().toString()
)