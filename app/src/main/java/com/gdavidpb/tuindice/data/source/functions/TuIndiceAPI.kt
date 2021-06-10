package com.gdavidpb.tuindice.data.source.functions

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.data.source.functions.responses.EnrollmentProofResponse
import com.gdavidpb.tuindice.data.source.functions.responses.SignInResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface TuIndiceAPI {
	@Headers("App-Version: ${BuildConfig.VERSION_CODE}")
	@POST("signIn")
	suspend fun signIn(
		@Header("Authorization") basicToken: String,
		@Header("Re-Authenticate") refreshToken: Boolean
	): Response<SignInResponse>

	@Headers("App-Version: ${BuildConfig.VERSION_CODE}")
	@POST("sync")
	suspend fun sync(): Response<Unit>

	@GET("enrollmentProof")
	suspend fun enrollmentProof(): Response<EnrollmentProofResponse>
}