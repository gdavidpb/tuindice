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
import com.gdavidpb.tuindice.platform.IosAttestationCapability
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class IosAttestationDataRepository(
	private val httpClientProvider: () -> HttpClient,
	private val attestationCapability: IosAttestationCapability
) : AttestationRepository {
	override suspend fun attest(request: AttestationRequest): Attestation {
		val httpClient = httpClientProvider()
		val requestHash = attestationCapability.sha256Base64Url(request.payloadJson)
			?: throw IllegalStateException("Unable to hash attestation payload on iOS.")
		val keyId = attestationCapability.resolveAttestationKeyId()
			?.takeIf { value -> value.isNotBlank() }
			?: throw IllegalStateException("Attestation keyId unavailable for iOS APP_ATTEST provider.")
		val session = httpClient
			.post("attestation/v2/sessions") {
				setBody(
					CreateAttestationSessionRequest(
						platform = PLATFORM_IOS,
						operationCode = request.operation.code,
						keyId = keyId
					)
				)
			}
			.body<CreateAttestationSessionResponse>()

		val bindingInput = attestationBindingInput(
			sessionId = session.sessionId,
			challenge = session.challenge,
			operation = request.operation,
			requestHash = requestHash
		)
		val bindingHash = attestationCapability.sha256Base64Url(bindingInput)
			?: throw IllegalStateException("Unable to hash attestation binding on iOS.")
		val providerAttestation = attestationCapability.requestAttestation(
			attestationInput = bindingHash,
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
					IssueAttestationTokenRequest(
						sessionId = session.sessionId,
						operationCode = request.operation.code,
						requestHash = requestHash,
						evidenceMode = session.evidenceMode,
						token = providerAttestation.token,
						keyId = keyId
					)
				)
			}
			.body<IssueAttestationTokenResponse>()

		return Attestation(token = response.token)
	}

	private companion object {
		const val PLATFORM_IOS = "iOS"
	}
}
