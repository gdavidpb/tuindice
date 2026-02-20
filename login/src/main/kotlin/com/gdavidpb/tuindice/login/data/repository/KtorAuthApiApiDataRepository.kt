package com.gdavidpb.tuindice.login.data.repository

import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.login.data.model.IssueTokensResponse
import com.gdavidpb.tuindice.login.data.model.RefreshTokensRequest
import com.gdavidpb.tuindice.login.data.model.RefreshTokensResponse
import com.gdavidpb.tuindice.login.domain.model.IssueTokens
import com.gdavidpb.tuindice.login.domain.model.RefreshTokens
import com.gdavidpb.tuindice.login.domain.repository.AuthApiRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import java.util.Base64

class KtorAuthApiApiDataRepository(
	private val ktorClient: HttpClient
) : AuthApiRepository {
	override suspend fun issueTokens(
		usbId: String,
		password: String,
		attestation: Attestation
	): IssueTokens {
		val credentials = Base64.getEncoder()
			.encodeToString("$usbId:$password".toByteArray())

		val response = ktorClient.post("auth/token") {
			basicAuth(usbId, password)
			header("X-Forwarded-Authorization", "Basic $credentials")

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

	override suspend fun refreshTokens(
		accessToken: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens {
		val request = RefreshTokensRequest(
			accessToken = accessToken,
			refreshToken = refreshToken
		)

		val response = ktorClient.post("auth/token/refresh") {
			header("Attestation-Id", attestation.id)
			header("Attestation", attestation.token)
			setBody(request)
		}.body<RefreshTokensResponse>()

		return RefreshTokens(
			accessToken = response.accessToken,
			refreshToken = response.refreshToken,
			expiresIn = response.expiresIn
		)
	}

	override suspend fun revokeTokens() {
		ktorClient.post("auth/token/revoke")
	}
}
