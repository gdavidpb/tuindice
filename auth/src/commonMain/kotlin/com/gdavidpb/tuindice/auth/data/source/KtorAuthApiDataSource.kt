package com.gdavidpb.tuindice.auth.data.source

import com.gdavidpb.tuindice.auth.data.model.IssueTokensResponse
import com.gdavidpb.tuindice.auth.data.model.RefreshTokensRequest
import com.gdavidpb.tuindice.auth.data.model.RefreshTokensResponse
import com.gdavidpb.tuindice.auth.data.contract.AuthApiDataSource
import com.gdavidpb.tuindice.auth.domain.model.IssueTokens
import com.gdavidpb.tuindice.auth.domain.model.AttestedTokenFlow
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.base.data.source.network.AttestationHeaders
import com.gdavidpb.tuindice.base.domain.model.Attestation
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class KtorAuthApiDataSource(
	private val ktorClient: HttpClient
) : AuthApiDataSource {
	override suspend fun issueTokens(
		usbId: String,
		password: String,
		attestedFlow: AttestedTokenFlow,
		attestation: Attestation
	): IssueTokens {
		return postTokens(
			usbId = usbId,
			password = password,
			attestedFlow = attestedFlow,
			attestation = attestation
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

		val response = ktorClient.post("auth/v1/token/refresh") {
			setAttestationTokenHeader(attestation)
			header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
			setBody(request)
		}.body<RefreshTokensResponse>()

		return RefreshTokens(
			accessToken = response.accessToken,
			refreshToken = response.refreshToken,
			expiresIn = response.expiresIn
		)
	}

	override suspend fun revokeTokens() {
		ktorClient.post("auth/v1/token/revoke")
	}

	private suspend fun postTokens(
		usbId: String,
		password: String,
		attestedFlow: AttestedTokenFlow,
		attestation: Attestation
	): IssueTokens {
		val response = ktorClient.post("auth/v1/token") {
			basicAuth(usbId, password)
			setAttestationTokenHeader(attestation)
			header(AttestationHeaders.ATTESTED_FLOW, attestedFlow.headerValue)
		}.body<IssueTokensResponse>()

		return IssueTokens(
			uid = response.uid,
			usbId = response.usbId,
			accessToken = response.accessToken,
			refreshToken = response.refreshToken,
			expiresIn = response.expiresIn
		)
	}

	private fun HttpRequestBuilder.setAttestationTokenHeader(attestation: Attestation) {
		header(AttestationHeaders.ATTESTATION_TOKEN, attestation.token)
	}
}
