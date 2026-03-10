package com.gdavidpb.tuindice.auth.data.source

import com.gdavidpb.tuindice.auth.data.model.IssueTokensResponse
import com.gdavidpb.tuindice.auth.data.model.RefreshTokensRequest
import com.gdavidpb.tuindice.auth.data.model.RefreshTokensResponse
import com.gdavidpb.tuindice.auth.data.repository.AuthApiDataSource
import com.gdavidpb.tuindice.auth.domain.model.IssueTokens
import com.gdavidpb.tuindice.auth.domain.model.IssueTokensFlow
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.base.domain.model.RiskAttestation
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
		flow: IssueTokensFlow,
		riskAttestation: RiskAttestation
	): IssueTokens {
		return postIssueTokens(
			usbId = usbId,
			password = password,
			flow = flow,
			riskAttestation = riskAttestation
		)
	}

	override suspend fun refreshTokens(
		accessToken: String,
		refreshToken: String,
		riskAttestation: RiskAttestation
	): RefreshTokens {
		val request = RefreshTokensRequest(
			accessToken = accessToken,
			refreshToken = refreshToken
		)

		val response = ktorClient.post("auth/v1/token/refresh") {
			setRiskAttestationHeaders(riskAttestation)
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

	private suspend fun postIssueTokens(
		usbId: String,
		password: String,
		flow: IssueTokensFlow,
		riskAttestation: RiskAttestation
	): IssueTokens {
		val response = ktorClient.post("auth/v1/token") {
			basicAuth(usbId, password)
			setRiskAttestationHeaders(riskAttestation)
			header(AuthHeaders.AUTH_FLOW, flow.headerValue)
		}.body<IssueTokensResponse>()

		return IssueTokens(
			uid = response.uid,
			usbId = response.usbId,
			accessToken = response.accessToken,
			refreshToken = response.refreshToken,
			expiresIn = response.expiresIn
		)
	}

	private fun HttpRequestBuilder.setRiskAttestationHeaders(riskAttestation: RiskAttestation) {
		header(AuthHeaders.RISK_ATTESTATION, riskAttestation.token)
	}

	private object AuthHeaders {
		const val RISK_ATTESTATION = "X-Risk-Attestation"
		const val AUTH_FLOW = "X-Auth-Flow"
	}
}
