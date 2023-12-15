package com.gdavidpb.tuindice.login.data.repository.login

import com.gdavidpb.tuindice.base.domain.annotation.AttestedApi
import com.gdavidpb.tuindice.base.domain.annotation.PublicApi
import com.gdavidpb.tuindice.login.data.repository.login.source.api.attestation.SignInAttestationParser
import com.gdavidpb.tuindice.login.data.repository.login.source.api.response.SignInResponse
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.POST

interface SignInApi {
	@PublicApi
	@AttestedApi(SignInAttestationParser::class)
	@POST("sign-in")
	suspend fun signIn(
		@Header("Authorization") basicToken: String
	): Response<SignInResponse>
}