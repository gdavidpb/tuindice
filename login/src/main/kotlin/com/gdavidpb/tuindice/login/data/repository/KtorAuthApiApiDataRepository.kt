package com.gdavidpb.tuindice.login.data.repository

import com.gdavidpb.tuindice.login.data.model.IssueTokensResponse
import com.gdavidpb.tuindice.login.domain.repository.AuthApiRepository
import com.gdavidpb.tuindice.login.domain.model.IssueTokens
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.post

class KtorAuthApiApiDataRepository(
	private val ktorClient: HttpClient
) : AuthApiRepository {
	override suspend fun issueTokens(usbId: String, password: String): IssueTokens {
		val response = ktorClient.post("auth/token") {
			basicAuth(usbId, password)
		}.body<IssueTokensResponse>()

		return IssueTokens(
			uid = response.uid,
			email = response.email,
			accessToken = response.accessToken,
			refreshToken = response.refreshToken,
			expiresIn = response.expiresIn
		)
	}

	override suspend fun revokeTokens() {
		ktorClient.post("auth/revoke")
	}
}