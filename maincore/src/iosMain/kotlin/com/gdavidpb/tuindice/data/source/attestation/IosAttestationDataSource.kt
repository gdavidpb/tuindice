package com.gdavidpb.tuindice.data.source.attestation

import com.gdavidpb.tuindice.base.data.model.AttestationPreparationRequiredResponse
import com.gdavidpb.tuindice.base.data.model.CompleteAttestationPreparationRequest
import com.gdavidpb.tuindice.base.data.model.CreateAttestationPreparationSessionRequest
import com.gdavidpb.tuindice.base.data.model.CreateAttestationSessionRequest
import com.gdavidpb.tuindice.base.data.model.CreateAttestationSessionResponse
import com.gdavidpb.tuindice.base.data.model.IssueAttestationTokenRequest
import com.gdavidpb.tuindice.base.data.model.IssueAttestationTokenResponse
import com.gdavidpb.tuindice.base.data.model.toRequestAuthorizationOrNull
import com.gdavidpb.tuindice.base.domain.model.AttestationAuthorization
import com.gdavidpb.tuindice.base.domain.model.AttestationPreparationCode
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationRequest
import com.gdavidpb.tuindice.base.domain.model.AttestationTemporarilyUnavailableException
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.utils.attestationBindingInput
import com.gdavidpb.tuindice.base.utils.extension.isForbidden
import com.gdavidpb.tuindice.base.utils.extension.isPreconditionRequired
import com.gdavidpb.tuindice.domain.model.IosPlatformAttestation
import com.gdavidpb.tuindice.platform.IosAttestationCapability
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class IosAttestationDataSource(
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
				runCatching {
					attestOnce(request = request, requestHash = requestHash)
				}.getOrElse { retryThrowable ->
					if (retryThrowable is RecoverableAppAttestException) {
						throw AttestationTemporarilyUnavailableException(
							platform = PLATFORM_IOS,
							operationCode = request.operationCode.value,
							cause = retryThrowable
						)
					}

					throw retryThrowable
				}
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
		val session = createOperationSession(
			httpClient = httpClient,
			operationCode = request.operationCode.value,
			keyId = keyId,
			authorization = request.authorization
		)

		val bindingHash = requireBindingHash(
			sessionId = session.sessionId,
			challenge = session.challenge,
			bindingCode = request.operationCode.value,
			requestHash = requestHash
		)
		val providerAttestation = requireAppAttestEvidence(
			bindingHash = bindingHash,
			keyId = keyId,
			evidenceMode = session.evidenceMode.value
		)

		val response = runCatching {
			httpClient
				.post(tokensPath()) {
					applyAttestationAuthorization(request.authorization)
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
			if (shouldRecoverFromForbidden(throwable)) {
				throw RecoverableAppAttestException(
					message = "App Attest evidence rejected by backend.",
					cause = throwable
				)
			}

			throw throwable
		}

		return Attestation(token = response.token)
	}

	private suspend fun createOperationSession(
		httpClient: HttpClient,
		operationCode: String,
		keyId: String,
		authorization: AttestationAuthorization
	): CreateAttestationSessionResponse {
		return try {
			requestOperationSession(
				httpClient = httpClient,
				operationCode = operationCode,
				keyId = keyId,
				authorization = authorization
			)
		} catch (throwable: Throwable) {
			val requiredPreparationCode = throwable.requiredPreparationCodeOrNull() ?: throw throwable
			prepareAttestation(
				httpClient = httpClient,
				preparationCode = requiredPreparationCode,
				keyId = keyId,
				authorization = authorization
			)
			requestOperationSession(
				httpClient = httpClient,
				operationCode = operationCode,
				keyId = keyId,
				authorization = authorization
			)
		}
	}

	private suspend fun requestOperationSession(
		httpClient: HttpClient,
		operationCode: String,
		keyId: String,
		authorization: AttestationAuthorization
	): CreateAttestationSessionResponse {
		return httpClient.post(operationSessionPath()) {
			applyAttestationAuthorization(authorization)
			setBody(
				CreateAttestationSessionRequest(
					platform = PLATFORM_IOS,
					operationCode = operationCode,
					authorization = authorization.toRequestAuthorizationOrNull(),
					keyId = keyId
				)
			)
		}.body()
	}

	private suspend fun prepareAttestation(
		httpClient: HttpClient,
		preparationCode: AttestationPreparationCode,
		keyId: String,
		authorization: AttestationAuthorization
	) {
		val requestHash = attestationCapability.sha256Base64Url(
			"""{"preparation_code":"${preparationCode.value}","key_id":"$keyId"}"""
		) ?: throw IllegalStateException("Unable to hash attestation preparation payload on iOS.")
		val session = httpClient.post(preparationSessionPath()) {
			applyAttestationAuthorization(authorization)
			setBody(
				CreateAttestationPreparationSessionRequest(
					platform = PLATFORM_IOS,
					preparationCode = preparationCode,
					authorization = authorization.toRequestAuthorizationOrNull(),
					keyId = keyId
				)
			)
		}.body<CreateAttestationSessionResponse>()
		val bindingHash = requireBindingHash(
			sessionId = session.sessionId,
			challenge = session.challenge,
			bindingCode = preparationCode.value,
			requestHash = requestHash
		)
		val providerAttestation = requireAppAttestEvidence(
			bindingHash = bindingHash,
			keyId = keyId,
			evidenceMode = session.evidenceMode.value
		)

		runCatching {
			httpClient.post(preparationCompletePath()) {
				applyAttestationAuthorization(authorization)
				setBody(
					CompleteAttestationPreparationRequest(
						sessionId = session.sessionId,
						preparationCode = preparationCode,
						requestHash = requestHash,
						evidenceMode = session.evidenceMode,
						token = providerAttestation.token,
						keyId = keyId
					)
				)
			}
		}.getOrElse { throwable ->
			if (shouldRecoverFromForbidden(throwable)) {
				throw RecoverableAppAttestException(
					message = "App Attest preparation evidence rejected by backend.",
					cause = throwable
				)
			}

			throw throwable
		}
	}

	private fun requireBindingHash(
		sessionId: String,
		challenge: String,
		bindingCode: String,
		requestHash: String
	): String {
		val bindingInput = attestationBindingInput(
			sessionId = sessionId,
			challenge = challenge,
			bindingCode = bindingCode,
			requestHash = requestHash
		)

		return attestationCapability.sha256Base64Url(bindingInput)
			?: throw IllegalStateException("Unable to hash attestation binding on iOS.")
	}

	private suspend fun requireAppAttestEvidence(
		bindingHash: String,
		keyId: String,
		evidenceMode: String
	): IosPlatformAttestation {
		val providerAttestation = runCatching {
			attestationCapability.requestAttestation(
				attestationInput = bindingHash,
				keyId = keyId,
				evidenceMode = evidenceMode
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

		return providerAttestation
	}

	private suspend fun Throwable.requiredPreparationCodeOrNull(): AttestationPreparationCode? {
		if (!isPreconditionRequired()) return null

		val exception = this as? ClientRequestException ?: return null
		return runCatching {
			exception.response.body<AttestationPreparationRequiredResponse>().requiredPreparationCode
		}.getOrNull()
	}

	private fun shouldRecoverFromForbidden(throwable: Throwable): Boolean {
		return throwable.isForbidden()
	}

	private fun HttpRequestBuilder.applyAttestationAuthorization(authorization: AttestationAuthorization) {
		if (authorization is AttestationAuthorization.Bearer) {
			bearerAuth(authorization.accessToken)
		}
	}

	private fun operationSessionPath(): String {
		return "attestation/v4/sessions"
	}

	private fun preparationSessionPath(): String {
		return "attestation/v4/preparations/sessions"
	}

	private fun tokensPath(): String {
		return "attestation/v4/tokens"
	}

	private fun preparationCompletePath(): String {
		return "attestation/v4/preparations/complete"
	}

	private companion object {
		const val PLATFORM_IOS = "iOS"
	}
}

private class RecoverableAppAttestException(
	message: String,
	cause: Throwable? = null
) : RuntimeException(message, cause)
