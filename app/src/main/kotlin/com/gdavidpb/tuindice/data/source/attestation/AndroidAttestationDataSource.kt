package com.gdavidpb.tuindice.data.source.attestation

import com.gdavidpb.tuindice.base.data.model.CreateAttestationPreparationSessionRequest
import com.gdavidpb.tuindice.base.data.model.toRequestAuthorizationOrNull
import com.gdavidpb.tuindice.base.data.model.AttestationPreparationRequiredResponse
import com.gdavidpb.tuindice.base.data.model.AttestationProofOfPossessionRequest
import com.gdavidpb.tuindice.base.data.model.CompleteAttestationPreparationRequest
import com.gdavidpb.tuindice.base.data.model.CreateAttestationSessionRequest
import com.gdavidpb.tuindice.base.data.model.CreateAttestationSessionResponse
import com.gdavidpb.tuindice.base.data.model.IssueAttestationTokenRequest
import com.gdavidpb.tuindice.base.data.model.IssueAttestationTokenResponse
import com.gdavidpb.tuindice.base.domain.model.AttestationAuthorization
import com.gdavidpb.tuindice.base.domain.model.AttestationEvidenceMode
import com.gdavidpb.tuindice.base.domain.model.AttestationPreparationCode
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationRequest
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.utils.attestationBindingInput
import com.gdavidpb.tuindice.base.utils.extension.isAttestationKeyUserMismatch
import com.gdavidpb.tuindice.base.utils.extension.isForbidden
import com.gdavidpb.tuindice.base.utils.extension.isPreconditionRequired
import com.gdavidpb.tuindice.base.utils.extension.isUnauthorized
import com.gdavidpb.tuindice.data.repository.attestation.AttestationProviderDataRepository
import com.gdavidpb.tuindice.platform.android.AndroidProofOfPossessionCapability
import com.gdavidpb.tuindice.platform.android.model.ProviderAttestation
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import java.security.MessageDigest
import kotlin.io.encoding.Base64
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private typealias UnauthorizedSessionRecovery = suspend (
	attemptedAuthorizationAccessToken: String?,
	attemptedCachedAccessToken: String?,
	attemptedCachedRefreshToken: String?
) -> SessionSnapshot?

class AndroidAttestationDataSource(
	private val ktorClient: HttpClient,
	private val providerDataSource: AttestationProviderDataRepository,
	private val proofOfPossessionCapability: AndroidProofOfPossessionCapability,
	private val configRepository: ConfigRepository,
	private val sessionRepository: SessionRepository? = null,
	private val recoverUnauthorizedSession: UnauthorizedSessionRecovery? = null
) : AttestationRepository {
	private val proofOfPossessionMutex = Mutex()

	override suspend fun attest(request: AttestationRequest): Attestation {
		val requestHash = sha256Base64Url(request.payloadJson)

		if (request.authorization is AttestationAuthorization.CurrentSession) {
			return attestCurrentSession(request = request, requestHash = requestHash)
		}

		return proofOfPossessionMutex.withLock {
			attestConfigured(request = request, requestHash = requestHash)
		}
	}

	private suspend fun attestCurrentSession(
		request: AttestationRequest,
		requestHash: String
	): Attestation {
		val attemptedSnapshot = requireNotNull(sessionRepository) {
			"SessionRepository is required for current-session attestation."
		}.getActiveSessionSnapshot()
			?: throw IllegalStateException("Active session is unavailable for current-session attestation.")

		return try {
			proofOfPossessionMutex.withLock {
				attestConfigured(
					request = request.withAccessToken(attemptedSnapshot.accessToken),
					requestHash = requestHash
				)
			}
		} catch (throwable: Throwable) {
			if (!throwable.isUnauthorized()) throw throwable

			val recoveredSnapshot = requireNotNull(recoverUnauthorizedSession) {
				"Session recovery is required for current-session attestation."
			}(
				attemptedSnapshot.accessToken,
				attemptedSnapshot.accessToken,
				attemptedSnapshot.refreshToken
			) ?: throw throwable

			proofOfPossessionMutex.withLock {
				attestConfigured(
					request = request.withAccessToken(recoveredSnapshot.accessToken),
					requestHash = requestHash
				)
			}
		}
	}

	private suspend fun attestConfigured(
		request: AttestationRequest,
		requestHash: String
	): Attestation {
		if (!configRepository.getAttestationAndroidEnforcementEnabled()) {
			return runCatching {
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

		return attestWithRecovery(
			request = request,
			requestHash = requestHash
		)
	}

	private fun AttestationRequest.withAccessToken(accessToken: String): AttestationRequest {
		return copy(
			authorization = AttestationAuthorization.Bearer(accessToken = accessToken)
		)
	}

	private suspend fun attestWithRecovery(
		request: AttestationRequest,
		requestHash: String
	): Attestation {
		return runCatching {
			attestOnce(
				request = request,
				requestHash = requestHash
			)
		}.recoverCatching { throwable ->
			if (throwable !is RecoverableAndroidKeystoreException) throw throwable

			proofOfPossessionCapability.invalidateProofOfPossessionKeyId()
			attestOnce(
				request = request,
				requestHash = requestHash
			)
		}.getOrThrow()
	}

	private suspend fun attestWithBypass(
		request: AttestationRequest,
		requestHash: String
	): Attestation {
		val keyId = BYPASS_KEY_ID
		val session = requestOperationSession(
			operationCode = request.operationCode.value,
			keyId = keyId,
			authorization = request.authorization
		)
		val bindingHash = bindingHash(
			sessionId = session.sessionId,
			challenge = session.challenge,
			bindingCode = request.operationCode.value,
			requestHash = requestHash
		)
		val response = ktorClient.post(tokensPath()) {
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

	private suspend fun Throwable.shouldFallbackToEnforcedAttestation(): Boolean {
		return isPreconditionRequired() || isForbidden() || isAttestationKeyUserMismatch()
	}

	private suspend fun attestOnce(
		request: AttestationRequest,
		requestHash: String
	): Attestation {
		val keyId = resolveKeyId()
		val session = createOperationSession(
			operationCode = request.operationCode.value,
			keyId = keyId,
			authorization = request.authorization
		)

		val bindingHash = bindingHash(
			sessionId = session.sessionId,
			challenge = session.challenge,
			bindingCode = request.operationCode.value,
			requestHash = requestHash
		)
		val providerAttestation = requirePlayIntegrityAttestation(
			bindingHash = bindingHash,
			evidenceMode = session.evidenceMode
		)
		val proofOfPossession = requireProofOfPossession(
			session = session,
			bindingHash = bindingHash,
			keyId = keyId,
			requireKeyAttestation = false
		)

		val response = try {
			ktorClient.post(tokensPath()) {
				applyAttestationAuthorization(request.authorization)
				setBody(
					IssueAttestationTokenRequest(
						sessionId = session.sessionId,
						operationCode = request.operationCode.value,
						requestHash = requestHash,
						evidenceMode = session.evidenceMode,
						token = providerAttestation.token,
						keyId = keyId,
						proofOfPossession = proofOfPossession
					)
				)
			}.body<IssueAttestationTokenResponse>()
		} catch (throwable: Throwable) {
			if (shouldRecoverFromKeyRotation(session, throwable)) {
				throw RecoverableAndroidKeystoreException(
					message = "Android proof-of-possession evidence rejected by backend.",
					cause = throwable
				)
			}

			throw throwable
		}

		return Attestation(token = response.token)
	}

	private suspend fun createOperationSession(
		operationCode: String,
		keyId: String,
		authorization: AttestationAuthorization
	): CreateAttestationSessionResponse {
		return try {
			requestOperationSession(
				operationCode = operationCode,
				keyId = keyId,
				authorization = authorization
			)
		} catch (throwable: Throwable) {
			if (throwable.isAttestationKeyUserMismatch()) {
				throw RecoverableAndroidKeystoreException(
					message = "Stored Android proof-of-possession key belongs to another user.",
					cause = throwable
				)
			}

			val requiredPreparationCode = throwable.requiredPreparationCodeOrNull() ?: throw throwable
			prepareAttestation(
				preparationCode = requiredPreparationCode,
				keyId = keyId,
				authorization = authorization
			)
			requestOperationSession(
				operationCode = operationCode,
				keyId = keyId,
				authorization = authorization
			)
		}
	}

	private suspend fun requestOperationSession(
		operationCode: String,
		keyId: String,
		authorization: AttestationAuthorization
	): CreateAttestationSessionResponse {
		return ktorClient.post(operationSessionPath()) {
			applyAttestationAuthorization(authorization)
			setBody(
				CreateAttestationSessionRequest(
					platform = PLATFORM_ANDROID,
					operationCode = operationCode,
					authorization = authorization.toRequestAuthorizationOrNull(),
					keyId = keyId
				)
			)
		}.body()
	}

	private suspend fun prepareAttestation(
		preparationCode: AttestationPreparationCode,
		keyId: String,
		authorization: AttestationAuthorization
	) {
		val requestHash = sha256Base64Url(
			"""{"preparation_code":"${preparationCode.value}","key_id":"$keyId"}"""
		)
		val session = try {
			ktorClient.post(preparationSessionPath()) {
				applyAttestationAuthorization(authorization)
				setBody(
					CreateAttestationPreparationSessionRequest(
						platform = PLATFORM_ANDROID,
						preparationCode = preparationCode,
						authorization = authorization.toRequestAuthorizationOrNull(),
						keyId = keyId
					)
				)
			}.body<CreateAttestationSessionResponse>()
		} catch (throwable: Throwable) {
			if (throwable.isAttestationKeyUserMismatch()) {
				throw RecoverableAndroidKeystoreException(
					message = "Stored Android proof-of-possession key belongs to another user.",
					cause = throwable
				)
			}

			throw throwable
		}
		val bindingHash = bindingHash(
			sessionId = session.sessionId,
			challenge = session.challenge,
			bindingCode = preparationCode.value,
			requestHash = requestHash
		)
		val providerAttestation = requirePlayIntegrityAttestation(
			bindingHash = bindingHash,
			evidenceMode = session.evidenceMode
		)
		val proofOfPossession = requireProofOfPossession(
			session = session,
			bindingHash = bindingHash,
			keyId = keyId,
			requireKeyAttestation = true
		)

		try {
			ktorClient.post(preparationCompletePath()) {
				applyAttestationAuthorization(authorization)
				setBody(
					CompleteAttestationPreparationRequest(
						sessionId = session.sessionId,
						preparationCode = preparationCode,
						requestHash = requestHash,
						evidenceMode = session.evidenceMode,
						token = providerAttestation.token,
						keyId = keyId,
						proofOfPossession = proofOfPossession
					)
				)
			}
		} catch (throwable: Throwable) {
			if (shouldRecoverFromKeyRotation(session, throwable)) {
				throw RecoverableAndroidKeystoreException(
					message = "Android preparation proof-of-possession evidence rejected by backend.",
					cause = throwable
				)
			}

			throw throwable
		}
	}

	private suspend fun requirePlayIntegrityAttestation(
		bindingHash: String,
		evidenceMode: AttestationEvidenceMode
	): ProviderAttestation {
		val providerAttestation = providerDataSource.getAttestation(
			bindingHash = bindingHash,
			evidenceMode = evidenceMode
		) ?: throw IllegalStateException("Play Integrity attestation unavailable.")

		check(providerAttestation.provider == AttestationProvider.PLAY_INTEGRITY) {
			"Unsupported Android attestation provider: ${providerAttestation.provider}."
		}

		return providerAttestation
	}

	private suspend fun requireProofOfPossession(
		session: CreateAttestationSessionResponse,
		bindingHash: String,
		keyId: String,
		requireKeyAttestation: Boolean
	): AttestationProofOfPossessionRequest? {
		return session.proofOfPossessionMode?.let {
			runCatching {
				proofOfPossessionCapability.createProofOfPossession(
					attestationInput = bindingHash,
					keyId = keyId,
					requireKeyAttestation = requireKeyAttestation
				)
			}.getOrElse { throwable ->
				throw RecoverableAndroidKeystoreException(
					message = "Unable to produce Android proof-of-possession evidence.",
					cause = throwable
				)
			}
		}
	}

	private fun bindingHash(
		sessionId: String,
		challenge: String,
		bindingCode: String,
		requestHash: String
	): String {
		return sha256Base64Url(
			attestationBindingInput(
				sessionId = sessionId,
				challenge = challenge,
				bindingCode = bindingCode,
				requestHash = requestHash
			)
		)
	}

	private suspend fun resolveKeyId(): String {
		return runCatching {
			proofOfPossessionCapability.resolveProofOfPossessionKeyId()
		}.getOrElse { throwable ->
			throw RecoverableAndroidKeystoreException(
				message = "Unable to resolve Android proof-of-possession keyId.",
				cause = throwable
			)
		}
			.takeIf { value -> value.isNotBlank() }
			?: throw RecoverableAndroidKeystoreException("Android proof-of-possession keyId unavailable.")
	}

	private fun sha256Base64Url(value: String): String {
		val digest = MessageDigest.getInstance("SHA-256")
			.digest(value.encodeToByteArray())

		return Base64
			.UrlSafe
			.withPadding(Base64.PaddingOption.ABSENT)
			.encode(digest)
	}

	private suspend fun Throwable.requiredPreparationCodeOrNull(): AttestationPreparationCode? {
		if (!isPreconditionRequired()) return null

		val exception = this as? ClientRequestException ?: return null
		return runCatching {
			exception.response.body<AttestationPreparationRequiredResponse>().requiredPreparationCode
		}.getOrNull()
	}

	private suspend fun shouldRecoverFromKeyRotation(
		session: CreateAttestationSessionResponse,
		throwable: Throwable
	): Boolean {
		return session.proofOfPossessionMode != null &&
				(throwable.isForbidden() || throwable.isAttestationKeyUserMismatch())
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
		const val PLATFORM_ANDROID = "Android"
		const val BYPASS_KEY_ID = "android-remote-config-bypass-key"
		const val BYPASS_TOKEN_PREFIX = "android-remote-config-bypass:"
	}
}

private class RecoverableAndroidKeystoreException(
	message: String,
	cause: Throwable? = null
) : RuntimeException(message, cause)
