package com.gdavidpb.tuindice.login.data.repository

import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.login.data.model.IssueTokensResponse
import com.gdavidpb.tuindice.login.data.model.RefreshTokensRequest
import com.gdavidpb.tuindice.login.data.model.RefreshTokensResponse
import com.gdavidpb.tuindice.login.domain.model.IssueTokens
import com.gdavidpb.tuindice.login.domain.model.RefreshTokens
import com.gdavidpb.tuindice.login.domain.repository.AuthApiRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.basicAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlin.io.encoding.Base64

class KtorAuthApiApiDataRepository(
	private val ktorClient: HttpClient
) : AuthApiRepository {
	override suspend fun issueTokens(
		usbId: String,
		password: String,
		attestation: Attestation
	): IssueTokens {
		val credentials = Base64.Default.encode("$usbId:$password".encodeToByteArray())

		val response = ktorClient.post("auth/token") {
			basicAuth(usbId, password)
			header(AuthHeaders.FORWARDED_AUTHORIZATION, "Basic $credentials")
			setAttestationHeaders(attestation)
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
			setAttestationHeaders(attestation)
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

	private fun HttpRequestBuilder.setAttestationHeaders(attestation: Attestation) {
		header(AuthHeaders.ATTESTATION_ID, attestation.id)
		header(AuthHeaders.ATTESTATION, attestation.token)
		header(AuthHeaders.ATTESTATION_PROVIDER, attestation.provider.name)

		val keyId = attestation.keyId?.takeIf { value -> value.isNotBlank() }
		if (attestation.provider == AttestationProvider.APP_ATTEST) {
			require(!keyId.isNullOrBlank()) {
				"${AuthHeaders.ATTESTATION_KEY_ID} is required for APP_ATTEST."
			}
		}

		keyId?.let {
			header(AuthHeaders.ATTESTATION_KEY_ID, it)
		}
	}

	private object AuthHeaders {
		const val FORWARDED_AUTHORIZATION = "X-Forwarded-Authorization"
		const val ATTESTATION_ID = "Attestation-Id"
		const val ATTESTATION = "Attestation"
		const val ATTESTATION_PROVIDER = "Attestation-Provider"
		const val ATTESTATION_KEY_ID = "Attestation-Key-Id"
	}
}
