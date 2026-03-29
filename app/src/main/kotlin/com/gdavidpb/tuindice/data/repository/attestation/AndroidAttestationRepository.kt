package com.gdavidpb.tuindice.data.repository.attestation

import com.gdavidpb.tuindice.base.data.model.CreateAttestationSessionRequest
import com.gdavidpb.tuindice.base.data.model.CreateAttestationSessionResponse
import com.gdavidpb.tuindice.base.data.model.IssueAttestationTokenRequest
import com.gdavidpb.tuindice.base.data.model.IssueAttestationTokenResponse
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationRequest
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.utils.attestationBindingInput
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import java.security.MessageDigest
import kotlin.io.encoding.Base64

class AndroidAttestationRepository(
	private val ktorClient: HttpClient,
	private val providerDataSource: AttestationProviderDataSource
) : AttestationRepository {
	override suspend fun attest(request: AttestationRequest): Attestation {
		val requestHash = sha256Base64Url(request.payloadJson)
		val session = ktorClient.post("attestation/v2/sessions") {
			setBody(
				CreateAttestationSessionRequest(
					platform = PLATFORM_ANDROID,
					operationCode = request.operation.code
				)
			)
		}.body<CreateAttestationSessionResponse>()

		val bindingHash = sha256Base64Url(
			attestationBindingInput(
				sessionId = session.sessionId,
				challenge = session.challenge,
				operation = request.operation,
				requestHash = requestHash
			)
		)
		val providerAttestation = providerDataSource.getAttestation(
			bindingHash = bindingHash,
			evidenceMode = session.evidenceMode
		)
			?: throw IllegalStateException("Play Integrity attestation unavailable.")
		check(providerAttestation.provider == AttestationProvider.PLAY_INTEGRITY) {
			"Unsupported Android attestation provider: ${providerAttestation.provider}."
		}

		val response = ktorClient.post("attestation/v2/tokens") {
			setBody(
				IssueAttestationTokenRequest(
					sessionId = session.sessionId,
					operationCode = request.operation.code,
					requestHash = requestHash,
					evidenceMode = session.evidenceMode,
					token = providerAttestation.token,
					keyId = providerAttestation.keyId
				)
			)
		}.body<IssueAttestationTokenResponse>()

		return Attestation(token = response.token)
	}

	private fun sha256Base64Url(value: String): String {
		val digest = MessageDigest.getInstance("SHA-256")
			.digest(value.encodeToByteArray())

		return Base64
			.UrlSafe
			.withPadding(Base64.PaddingOption.ABSENT)
			.encode(digest)
	}

	private companion object {
		const val PLATFORM_ANDROID = "Android"
	}
}
