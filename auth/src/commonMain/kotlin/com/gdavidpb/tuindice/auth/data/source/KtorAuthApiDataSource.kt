package com.gdavidpb.tuindice.auth.data.source

import com.gdavidpb.tuindice.auth.data.model.BootstrapTokensResponse
import com.gdavidpb.tuindice.auth.data.model.IssueTokensResponse
import com.gdavidpb.tuindice.auth.data.model.RefreshTokensRequest
import com.gdavidpb.tuindice.auth.data.model.RefreshTokensResponse
import com.gdavidpb.tuindice.auth.data.model.RevokeTokensRequest
import com.gdavidpb.tuindice.auth.data.repository.AuthApiDataRepository
import com.gdavidpb.tuindice.auth.domain.model.AttestedTokenFlow
import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.IssueTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.repository.AuthRetryWindowRepository
import com.gdavidpb.tuindice.auth.utils.extension.toCanonicalUsbIdentifier
import com.gdavidpb.tuindice.base.domain.exception.ServiceRetryWindowException
import com.gdavidpb.tuindice.security.data.source.network.AttestationHeaders
import com.gdavidpb.tuindice.security.domain.model.Attestation
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.basicAuth
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode

class KtorAuthApiDataSource(
	private val ktorClient: HttpClient,
	private val retryWindow: AuthRetryWindowRepository
) : AuthApiDataRepository {
	override suspend fun bootstrapSignIn(
		usbId: String,
		password: String
	): BootstrapTokens {
		// Keyed by the canonical account so sign-in and the session recovery share one wait however
		// each of them spells it.
		val response = withRetryWindow(key = usbId.toCanonicalUsbIdentifier()) {
			ktorClient.post("auth/v2/bootstrap") {
				basicAuth(usbId, password)
			}.body<BootstrapTokensResponse>()
		}

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
		val response = withRetryWindow(key = AuthRetryWindowRepository.EXCHANGE_KEY) {
			ktorClient.post("auth/v2/token/exchange") {
				bearerAuth(bootstrapAccessToken)
				setAttestationTokenHeader(attestation)
			}.body<IssueTokensResponse>()
		}

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
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens {
		val request = RefreshTokensRequest(
			sessionId = sessionId,
			refreshToken = refreshToken
		)

		val response = ktorClient.post("auth/v2/token/refresh") {
			setAttestationTokenHeader(attestation)
			header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
			setBody(request)
		}.body<RefreshTokensResponse>()

		return RefreshTokens(
			sessionId = response.sessionId,
			accessToken = response.accessToken,
			refreshToken = response.refreshToken,
			expiresIn = response.expiresIn
		)
	}

	override suspend fun revokeTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	) {
		ktorClient.post("auth/v2/token/revoke") {
			setAttestationTokenHeader(attestation)
			setBody(
				RevokeTokensRequest(
					sessionId = sessionId,
					refreshToken = refreshToken
				)
			)
		}
	}

	/**
	 * Bootstrap and the token exchange answer 503 and 429 with a `Retry-After`: the wait is
	 * remembered, and a call made inside it fails without touching the network. The original
	 * response exception still reaches the caller, so every handler keeps classifying it.
	 */
	private suspend fun <T> withRetryWindow(key: String, call: suspend () -> T): T {
		val remainingMillis = retryWindow.remainingMillis(key)

		if (remainingMillis > 0) throw ServiceRetryWindowException(retryAfterMillis = remainingMillis)

		return try {
			call().also { retryWindow.recordSuccess(key) }
		} catch (exception: ResponseException) {
			val retryAfterSeconds = exception.response.headers[HttpHeaders.RetryAfter]?.trim()?.toLongOrNull()

			when (exception.response.status) {
				HttpStatusCode.ServiceUnavailable -> retryWindow.recordUnavailable(key, retryAfterSeconds)
				HttpStatusCode.TooManyRequests -> retryWindow.recordTooManyRequests(key, retryAfterSeconds)
			}

			throw exception
		}
	}

	private fun IssueTokensResponse.toIssueTokens(): IssueTokens {
		return IssueTokens(
			uid = uid,
			usbId = usbId,
			sessionId = sessionId,
			accessToken = accessToken,
			refreshToken = refreshToken,
			expiresIn = expiresIn
		)
	}

	private fun HttpRequestBuilder.setAttestationTokenHeader(attestation: Attestation) {
		header(AttestationHeaders.ATTESTATION_TOKEN, attestation.token)
	}
}
