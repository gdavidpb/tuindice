package com.gdavidpb.tuindice.login.data.repository.login

import com.gdavidpb.tuindice.base.domain.annotation.AttestedApi
import com.gdavidpb.tuindice.base.domain.annotation.PublicApi
import com.gdavidpb.tuindice.login.data.repository.login.source.api.attestation.SignInAttestation
import com.gdavidpb.tuindice.login.data.repository.login.source.api.response.SignInResponse
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.POST

interface SignInApi {
	@PublicApi
	@AttestedApi(SignInAttestation::class)
	@POST("signIn")
	suspend fun signIn(
		@Header("Authorization") basicToken: String,
		@Header("Re-Authenticate") refreshToken: Boolean
	): Response<SignInResponse>
}