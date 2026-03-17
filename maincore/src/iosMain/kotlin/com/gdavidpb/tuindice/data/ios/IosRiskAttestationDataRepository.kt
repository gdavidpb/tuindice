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
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class IosRiskAttestationDataRepository(
	private val httpClientProvider: () -> HttpClient,
	private val attestationCapability: IosAttestationCapability
) : RiskAttestationRepository {
	override suspend fun issueProof(request: RiskAttestationRequest): RiskAttestation {
		val httpClient = httpClientProvider()
		val requestHash = attestationCapability.sha256Base64Url(request.payloadJson)
			?: throw IllegalStateException("Unable to hash risk attestation payload on iOS.")
		val keyId = attestationCapability.resolveAttestationKeyId()
			?.takeIf { value -> value.isNotBlank() }
			?: throw IllegalStateException("Attestation keyId unavailable for iOS APP_ATTEST provider.")
		val session = httpClient
			.post("attestation/v2/sessions") {
				setBody(
					CreateRiskAttestationSessionRequest(
						platform = PLATFORM_IOS,
						keyId = keyId
					)
				)
			}
			.body<CreateRiskAttestationSessionResponse>()

		val bindingInput = "${session.sessionId}:${session.challenge}:${request.operation.code}:$requestHash"
		val bindingValue = attestationCapability.sha256Base64Url(bindingInput)
			?: throw IllegalStateException("Unable to hash risk attestation binding on iOS.")
		val providerAttestation = attestationCapability.requestAttestation(
			attestationInput = bindingValue,
			keyId = keyId,
			evidenceMode = session.evidenceMode.value
		)
			?: throw IllegalStateException("App Attest evidence unavailable on iOS bridge.")
		check(providerAttestation.provider == AttestationProvider.APP_ATTEST) {
			"Unsupported iOS attestation provider: ${providerAttestation.provider}."
		}
		check(providerAttestation.keyId == keyId) {
			"Unexpected iOS attestation keyId returned by bridge."
		}

		val response = httpClient
			.post("attestation/v2/tokens") {
				setBody(
					IssueRiskAttestationTokenRequest(
						sessionId = session.sessionId,
						operationCode = request.operation.code,
						requestHash = requestHash,
						evidenceMode = session.evidenceMode,
						token = providerAttestation.token,
						keyId = keyId
					)
				)
			}
			.body<IssueRiskAttestationTokenResponse>()

		return RiskAttestation(token = response.token)
	}

	private companion object {
		const val PLATFORM_IOS = "iOS"
	}
}
