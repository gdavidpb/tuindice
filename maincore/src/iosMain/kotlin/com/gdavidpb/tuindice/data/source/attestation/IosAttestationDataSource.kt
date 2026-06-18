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
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.utils.attestationBindingInput
import com.gdavidpb.tuindice.base.utils.extension.isAttestationKeyUserMismatch
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
	private val httpClient: HttpClient,
	private val attestationCapability: IosAttestationCapability,
	private val configRepository: ConfigRepository
) : AttestationRepository {
	private val appAttestMutex = Mutex()

	override suspend fun attest(request: AttestationRequest): Attestation {
		return appAttestMutex.withLock {
			val requestHash = attestationCapability.sha256Base64Url(request.payloadJson)
				?: throw IllegalStateException("Unable to hash attestation payload on iOS.")

			if (!configRepository.getAttestationIosEnforcementEnabled()) {
				return@withLock runCatching {
					attestWithBypass(
						request = request,
						requestHash = requestHash
					)
				}.recoverCatching { throwable ->
					if (!throwable.shouldFallbackToEnforcedAttestation()) throw throwable
					attestWithRecovery(
						request = request,
						requestHash = requestHash
					)
				}.getOrThrow()
			}

			attestWithRecovery(
				request = request,
				requestHash = requestHash
			)
		}
	}

	private suspend fun attestWithRecovery(
		request: AttestationRequest,
		requestHash: String
	): Attestation {
		return runCatching {
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

	private suspend fun attestWithBypass(
		request: AttestationRequest,
		requestHash: String
	): Attestation {
		val keyId = BYPASS_KEY_ID
		val session = requestOperationSession(
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
		val response = httpClient.post(tokensPath()) {
			applyAttestationAuthorization(request.authorization)
			setBody(
				IssueAttestationTokenRequest(
					sessionId = session.sessionId,
					operationCode = request.operationCode.value,
					requestHash = requestHash,
					evidenceMode = session.evidenceMode,
					token = "$BYPASS_TOKEN_PREFIX$bindingHash",
					keyId = keyId
				)
			)
		}.body<IssueAttestationTokenResponse>()

		return Attestation(token = response.token)
	}

	private suspend fun attestOnce(
		request: AttestationRequest,
		requestHash: String
	): Attestation {
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

		val response = try {
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
		} catch (throwable: Throwable) {
			if (shouldRecoverFromKeyRotation(throwable)) {
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
			if (throwable.isAttestationKeyUserMismatch()) {
				throw RecoverableAppAttestException(
					message = "Stored App Attest key belongs to another user.",
					cause = throwable
				)
			}

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
		val session = try {
			httpClient.post(preparationSessionPath()) {
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
		} catch (throwable: Throwable) {
			if (throwable.isAttestationKeyUserMismatch()) {
				throw RecoverableAppAttestException(
					message = "Stored App Attest key belongs to another user.",
					cause = throwable
				)
			}

			throw throwable
		}
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

		try {
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
		} catch (throwable: Throwable) {
			if (shouldRecoverFromKeyRotation(throwable)) {
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

	private suspend fun shouldRecoverFromKeyRotation(throwable: Throwable): Boolean {
		return throwable.isForbidden() || throwable.isAttestationKeyUserMismatch()
	}

	private suspend fun Throwable.shouldFallbackToEnforcedAttestation(): Boolean {
		return isPreconditionRequired() || isForbidden() || isAttestationKeyUserMismatch()
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
		const val BYPASS_KEY_ID = "ios-remote-config-bypass-key"
		const val BYPASS_TOKEN_PREFIX = "ios-remote-config-bypass:"
	}
}

private class RecoverableAppAttestException(
	message: String,
	cause: Throwable? = null
) : RuntimeException(message, cause)
