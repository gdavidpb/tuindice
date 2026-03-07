package com.gdavidpb.tuindice.data.repository.attestation

import com.gdavidpb.tuindice.base.data.model.CreateRiskAttestationSessionRequest
import com.gdavidpb.tuindice.base.data.model.CreateRiskAttestationSessionResponse
import com.gdavidpb.tuindice.base.data.model.IssueRiskAttestationTokenRequest
import com.gdavidpb.tuindice.base.data.model.IssueRiskAttestationTokenResponse
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.domain.model.RiskAttestation
import com.gdavidpb.tuindice.base.domain.model.RiskAttestationRequest
import com.gdavidpb.tuindice.base.domain.repository.RiskAttestationRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import java.security.MessageDigest
import kotlin.io.encoding.Base64

class RiskAttestationDataRepository(
	private val ktorClient: HttpClient,
	private val providerDataSource: AttestationProviderDataSource
) : RiskAttestationRepository {
	override suspend fun issueProof(request: RiskAttestationRequest): RiskAttestation {
		val requestHash = sha256Base64Url(request.payloadJson)
		val session = ktorClient.post("attestation/v1/sessions") {
			setBody(
				CreateRiskAttestationSessionRequest(
					platform = PLATFORM_ANDROID
				)
			)
		}.body<CreateRiskAttestationSessionResponse>()

		val bindingValue = sha256Base64Url(
			"${session.sessionId}:${session.challenge}:${request.operation.code}:$requestHash"
		)
		val providerAttestation = providerDataSource.getAttestation(bindingValue)
			?: throw IllegalStateException("Play Integrity attestation unavailable.")
		check(providerAttestation.provider == AttestationProvider.PLAY_INTEGRITY) {
			"Unsupported Android attestation provider: ${providerAttestation.provider}."
		}

		val response = ktorClient.post("attestation/v1/tokens") {
			setBody(
				IssueRiskAttestationTokenRequest(
					sessionId = session.sessionId,
					operationCode = request.operation.code,
					requestHash = requestHash,
					token = providerAttestation.token,
					keyId = providerAttestation.keyId
				)
			)
		}.body<IssueRiskAttestationTokenResponse>()

		return RiskAttestation(token = response.token)
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
