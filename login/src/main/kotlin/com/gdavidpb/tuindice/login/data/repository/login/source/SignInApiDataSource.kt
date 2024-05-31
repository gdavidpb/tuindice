package com.gdavidpb.tuindice.login.data.repository.login.source

import com.gdavidpb.tuindice.login.data.repository.login.RemoteDataSource
import com.gdavidpb.tuindice.login.data.repository.login.source.api.mapper.toSignIn
import com.gdavidpb.tuindice.login.data.repository.login.source.api.response.SignInResponse
import com.gdavidpb.tuindice.login.domain.model.SignIn
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.header
import io.ktor.client.request.post

class SignInApiDataSource(
	private val ktorClient: HttpClient
) : RemoteDataSource {
	override suspend fun signIn(
		username: String,
		password: String,
		attestation: String
	): SignIn {
		return ktorClient.post("sign-in") {
			basicAuth(username, password)

			header("Attestation", attestation)
		}
			.body<SignInResponse>()
			.toSignIn()
	}
}