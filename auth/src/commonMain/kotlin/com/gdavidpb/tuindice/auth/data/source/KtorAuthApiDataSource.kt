package com.gdavidpb.tuindice.auth.data.source

import com.gdavidpb.tuindice.auth.data.model.BootstrapTokensResponse
import com.gdavidpb.tuindice.auth.data.model.IssueTokensResponse
import com.gdavidpb.tuindice.auth.data.model.RefreshTokensRequest
import com.gdavidpb.tuindice.auth.data.model.RefreshTokensResponse
import com.gdavidpb.tuindice.auth.data.repository.AuthApiDataRepository
import com.gdavidpb.tuindice.auth.domain.model.AttestedTokenFlow
import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.IssueTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.base.data.source.network.AttestationHeaders
import com.gdavidpb.tuindice.base.domain.model.Attestation
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.basicAuth
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

class KtorAuthApiDataSource(
	private val ktorClient: HttpClient
) : AuthApiDataRepository {
	override suspend fun bootstrapSignIn(
		usbId: String,
		password: String
	): BootstrapTokens {
		val response = ktorClient.post("auth/v2/bootstrap") {
			basicAuth(usbId, password)
		}.body<BootstrapTokensResponse>()

		return BootstrapTokens(
			uid = response.uid,
			usbId = response.usbId,
			accessToken = response.accessToken,
			expiresIn = response.expiresIn
		)
	}

	override suspend fun exchangeSignIn(
		bootstrapAccessToken: String,
		attestation: Attestation
	): IssueTokens {
		val response = ktorClient.post("auth/v2/token/exchange") {
			bearerAuth(bootstrapAccessToken)
			setAttestationTokenHeader(attestation)
		}.body<IssueTokensResponse>()

		return response.toIssueTokens()
	}

	override suspend fun reissueTokens(
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

		return response.toIssueTokens()
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

	override suspend fun revokeTokens(accessToken: String) {
		ktorClient.post("auth/v1/token/revoke") {
			bearerAuth(accessToken)
		}
	}

	private fun IssueTokensResponse.toIssueTokens(): IssueTokens {
		return IssueTokens(
			uid = uid,
			usbId = usbId,
			accessToken = accessToken,
			refreshToken = refreshToken,
			expiresIn = expiresIn
		)
	}

	private fun HttpRequestBuilder.setAttestationTokenHeader(attestation: Attestation) {
		header(AttestationHeaders.ATTESTATION_TOKEN, attestation.token)
	}
}
