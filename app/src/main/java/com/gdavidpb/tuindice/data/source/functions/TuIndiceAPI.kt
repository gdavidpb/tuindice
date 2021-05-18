package com.gdavidpb.tuindice.data.source.functions

import com.gdavidpb.tuindice.data.source.functions.requests.CheckCredentialsRequest
import com.gdavidpb.tuindice.data.source.functions.requests.SignInRequest
import com.gdavidpb.tuindice.data.source.functions.responses.SignInResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TuIndiceAPI {
	@POST("signIn")
	suspend fun signIn(
		@Body request: SignInRequest
	): Response<SignInResponse>

	@POST("checkCredentials")
	suspend fun checkCredentials(
		@Body request: CheckCredentialsRequest
	): Response<Unit>
}