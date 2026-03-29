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
import com.gdavidpb.tuindice.base.utils.extension.isForbidden
import com.gdavidpb.tuindice.platform.IosAttestationCapability
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class IosAttestationDataRepository(
	private val httpClientProvider: () -> HttpClient,
	private val attestationCapability: IosAttestationCapability
) : AttestationRepository {
	private val appAttestMutex = Mutex()

	override suspend fun attest(request: AttestationRequest): Attestation {
		return appAttestMutex.withLock {
			val requestHash = attestationCapability.sha256Base64Url(request.payloadJson)
				?: throw IllegalStateException("Unable to hash attestation payload on iOS.")

			runCatching {
				attestOnce(request = request, requestHash = requestHash)
			}.recoverCatching { throwable ->
				if (throwable !is RecoverableAppAttestException) throw throwable

				attestationCapability.invalidateAttestationKeyId()
				attestOnce(request = request, requestHash = requestHash)
			}.getOrThrow()
		}
	}

	private suspend fun attestOnce(
		request: AttestationRequest,
		requestHash: String
	): Attestation {
		val httpClient = httpClientProvider()
		val keyId = runCatching {
			attestationCapability.resolveAttestationKeyId()
		}.getOrElse { throwable ->
			throw RecoverableAppAttestException(
				message = "Unable to resolve App Attest keyId on iOS.",
				cause = throwable
			)
		}
			?.takeIf { value -> value.isNotBlank() }
			?: throw RecoverableAppAttestException("Attestation keyId unavailable for iOS APP_ATTEST provider.")
		val session = httpClient
			.post("attestation/v2/sessions") {
				setBody(
					CreateAttestationSessionRequest(
						platform = PLATFORM_IOS,
						operationCode = request.operationCode.value,
						keyId = keyId
					)
				)
			}
			.body<CreateAttestationSessionResponse>()

		val bindingInput = attestationBindingInput(
			sessionId = session.sessionId,
			challenge = session.challenge,
			operationCode = request.operationCode,
			requestHash = requestHash
		)
		val bindingHash = attestationCapability.sha256Base64Url(bindingInput)
			?: throw IllegalStateException("Unable to hash attestation binding on iOS.")
		val providerAttestation = runCatching {
			attestationCapability.requestAttestation(
				attestationInput = bindingHash,
				keyId = keyId,
				evidenceMode = session.evidenceMode.value
			)
		}.getOrElse { throwable ->
			throw RecoverableAppAttestException(
				message = "Unable to produce App Attest evidence on iOS.",
				cause = throwable
			)
		}
			?: throw RecoverableAppAttestException("App Attest evidence unavailable on iOS bridge.")
		check(providerAttestation.provider == AttestationProvider.APP_ATTEST) {
			"Unsupported iOS attestation provider: ${providerAttestation.provider}."
		}
		check(providerAttestation.keyId == keyId) {
			"Unexpected iOS attestation keyId returned by bridge."
		}

		val response = runCatching {
			httpClient
				.post("attestation/v2/tokens") {
					setBody(
						IssueAttestationTokenRequest(
							sessionId = session.sessionId,
							operationCode = request.operationCode.value,
							requestHash = requestHash,
							evidenceMode = session.evidenceMode,
							token = providerAttestation.token,
							keyId = keyId
						)
					)
				}
				.body<IssueAttestationTokenResponse>()
		}.getOrElse { throwable ->
			if (throwable.isForbidden()) {
				throw RecoverableAppAttestException(
					message = "App Attest evidence rejected by backend.",
					cause = throwable
				)
			}

			throw throwable
		}

		return Attestation(token = response.token)
	}

	private companion object {
		const val PLATFORM_IOS = "iOS"
	}
}

private class RecoverableAppAttestException(
	message: String,
	cause: Throwable? = null
) : RuntimeException(message, cause)
