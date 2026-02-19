package com.gdavidpb.tuindice.login.data.repository

import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.login.data.model.IssueTokensResponse
import com.gdavidpb.tuindice.login.data.model.RefreshTokenRequest
import com.gdavidpb.tuindice.login.domain.model.IssueTokens
import com.gdavidpb.tuindice.login.domain.repository.AuthApiRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class KtorAuthApiApiDataRepository(
	private val ktorClient: HttpClient
) : AuthApiRepository {
	override suspend fun issueTokens(
		usbId: String,
		password: String,
		attestation: Attestation
	): IssueTokens {
		val response = ktorClient.post("auth/token") {
			basicAuth(usbId, password)

			header("Attestation-Id", attestation.id)
			header("Attestation", attestation.token)
		}.body<IssueTokensResponse>()

		return IssueTokens(
			uid = response.uid,
			usbId = response.usbId,
			accessToken = response.accessToken,
			refreshToken = response.refreshToken,
			expiresIn = response.expiresIn
		)
	}

	override suspend fun refreshTokens(accessToken: String, refreshToken: String): IssueTokens {
		val request = RefreshTokenRequest(
			accessToken = accessToken,
			refreshToken = refreshToken
		)

		val response = ktorClient.post("auth/token/refresh") {
			setBody(request)
		}.body<IssueTokensResponse>()

		return IssueTokens(
			uid = response.uid,
			usbId = response.usbId,
			accessToken = response.accessToken,
			refreshToken = response.refreshToken,
			expiresIn = response.expiresIn
		)
	}

	override suspend fun revokeTokens() {
		ktorClient.post("auth/token/revoke")
	}
}