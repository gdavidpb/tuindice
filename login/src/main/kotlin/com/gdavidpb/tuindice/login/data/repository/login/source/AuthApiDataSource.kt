package com.gdavidpb.tuindice.login.data.repository.login.source

import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.login.data.repository.login.RemoteDataSource
import com.gdavidpb.tuindice.login.data.repository.login.source.api.response.SignInResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.header
import io.ktor.client.request.post

class AuthApiDataSource(
	private val ktorClient: HttpClient
) : RemoteDataSource {
	override suspend fun auth(
		username: String,
		password: String,
		attestation: Attestation
	): String {
		return ktorClient.post("auth") {
			basicAuth(username, password)

			header("Attestation-Id", attestation.id)
			header("Attestation", attestation.token)
		}
			.body<SignInResponse>()
			.token
	}
}