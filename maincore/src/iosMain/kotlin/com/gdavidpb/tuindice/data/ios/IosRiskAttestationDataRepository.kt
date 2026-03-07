package com.gdavidpb.tuindice.data.ios

import com.gdavidpb.tuindice.base.data.model.CreateRiskAttestationSessionRequest
import com.gdavidpb.tuindice.base.data.model.CreateRiskAttestationSessionResponse
import com.gdavidpb.tuindice.base.data.model.IssueRiskAttestationTokenRequest
import com.gdavidpb.tuindice.base.data.model.IssueRiskAttestationTokenResponse
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.domain.model.RiskAttestation
import com.gdavidpb.tuindice.base.domain.model.RiskAttestationRequest
import com.gdavidpb.tuindice.base.domain.repository.RiskAttestationRepository
import com.gdavidpb.tuindice.platform.ios.IosAttestationCapability
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

internal class IosRiskAttestationDataRepository(
	private val httpClientProvider: () -> HttpClient,
	private val attestationCapability: IosAttestationCapability
) : RiskAttestationRepository {
	override suspend fun issueProof(request: RiskAttestationRequest): RiskAttestation {
		val httpClient = httpClientProvider()
		val requestHash = attestationCapability.sha256Base64Url(request.payloadJson)
			?: throw IllegalStateException("Unable to hash risk attestation payload on iOS.")
		val session = httpClient
			.post("attestation/v1/sessions") {
				setBody(
					CreateRiskAttestationSessionRequest(
						platform = PLATFORM_IOS
					)
				)
			}
			.body<CreateRiskAttestationSessionResponse>()

		val bindingInput = "${session.sessionId}:${session.challenge}:${request.operation.code}:$requestHash"
		val bindingValue = attestationCapability.sha256Base64Url(bindingInput)
			?: throw IllegalStateException("Unable to hash risk attestation binding on iOS.")
		val providerAttestation = attestationCapability.requestAttestation(bindingValue)
			?: throw IllegalStateException("App Attest evidence unavailable on iOS bridge.")
		check(providerAttestation.provider == AttestationProvider.APP_ATTEST) {
			"Unsupported iOS attestation provider: ${providerAttestation.provider}."
		}
		val keyId = providerAttestation.keyId
			?.takeIf { value -> value.isNotBlank() }
			?: throw IllegalStateException("Attestation keyId unavailable for iOS APP_ATTEST provider.")

		val response = httpClient
			.post("attestation/v1/tokens") {
				setBody(
					IssueRiskAttestationTokenRequest(
						sessionId = session.sessionId,
						operationCode = request.operation.code,
						requestHash = requestHash,
						token = providerAttestation.token,
						keyId = keyId
					)
				)
			}
			.body<IssueRiskAttestationTokenResponse>()

		if (providerAttestation.isInitialKeyAttestation) {
			attestationCapability.markAttestationKeyRegistered(keyId)
		}

		return RiskAttestation(token = response.token)
	}

	private companion object {
		const val PLATFORM_IOS = "iOS"
	}
}
