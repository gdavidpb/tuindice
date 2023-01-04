package com.gdavidpb.tuindice.login.data.source.api

import com.gdavidpb.tuindice.login.data.source.api.responses.SignInResponse
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface SignInApi {
	@POST("signIn")
	suspend fun signIn(
		@Header("Authorization") basicToken: String,
		@Header("Re-Authenticate") refreshToken: Boolean,
		@Query("messagingToken") messagingToken: String? = null
	): Response<SignInResponse>
}