package com.gdavidpb.tuindice.data.source.attestation

import com.gdavidpb.tuindice.base.data.model.CreateAttestationSessionRequest
import com.gdavidpb.tuindice.base.data.model.CreateAttestationPreparationSessionRequest
import com.gdavidpb.tuindice.base.data.model.CreateAttestationSessionResponse
import com.gdavidpb.tuindice.base.data.model.AttestationPreparationRequiredResponse
import com.gdavidpb.tuindice.base.data.model.CompleteAttestationPreparationRequest
import com.gdavidpb.tuindice.base.data.model.IssueAttestationTokenRequest
import com.gdavidpb.tuindice.base.data.model.IssueAttestationTokenResponse
import com.gdavidpb.tuindice.base.domain.model.AttestationEvidenceMode
import com.gdavidpb.tuindice.base.domain.model.AttestationPreparationCode
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationRequest
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.utils.attestationBindingInput
import com.gdavidpb.tuindice.base.utils.extension.isForbidden
import com.gdavidpb.tuindice.base.utils.extension.isPreconditionRequired
import com.gdavidpb.tuindice.data.repository.attestation.AttestationProviderDataRepository
import com.gdavidpb.tuindice.platform.android.AndroidProofOfPossessionCapability
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import java.security.MessageDigest
import kotlin.io.encoding.Base64
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AndroidAttestationDataSource(
	private val ktorClient: HttpClient,
	private val providerDataSource: AttestationProviderDataRepository,
	private val proofOfPossessionCapability: AndroidProofOfPossessionCapability
) : AttestationRepository {
	private val proofOfPossessionMutex = Mutex()

	override suspend fun attest(request: AttestationRequest): Attestation {
		return proofOfPossessionMutex.withLock {
			val requestHash = sha256Base64Url(request.payloadJson)

			runCatching {
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
	}

	private suspend fun attestOnce(
		request: AttestationRequest,
		requestHash: String
	): Attestation {
		val keyId = resolveKeyId()
		val session = createOperationSession(
			operationCode = request.operationCode.value,
			keyId = keyId,
			bearerToken = request.bearerToken
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

		val response = runCatching {
			ktorClient.post("attestation/v3/tokens") {
				bearerAuth(request.bearerToken)
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
		}.getOrElse { throwable ->
			if (shouldRecoverFromForbidden(session, throwable)) {
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
		bearerToken: String
	): CreateAttestationSessionResponse {
		return try {
			requestOperationSession(
				operationCode = operationCode,
				keyId = keyId,
				bearerToken = bearerToken
			)
		} catch (throwable: Throwable) {
			val requiredPreparationCode = throwable.requiredPreparationCodeOrNull() ?: throw throwable
			prepareAttestation(
				preparationCode = requiredPreparationCode,
				keyId = keyId,
				bearerToken = bearerToken
			)
			requestOperationSession(
				operationCode = operationCode,
				keyId = keyId,
				bearerToken = bearerToken
			)
		}
	}

	private suspend fun requestOperationSession(
		operationCode: String,
		keyId: String,
		bearerToken: String
	): CreateAttestationSessionResponse {
		return ktorClient.post("attestation/v3/sessions") {
			bearerAuth(bearerToken)
			setBody(
				CreateAttestationSessionRequest(
					platform = PLATFORM_ANDROID,
					operationCode = operationCode,
					keyId = keyId
				)
			)
		}.body()
	}

	private suspend fun prepareAttestation(
		preparationCode: AttestationPreparationCode,
		keyId: String,
		bearerToken: String
	) {
		val requestHash = sha256Base64Url(
			"""{"preparation_code":"${preparationCode.value}","key_id":"$keyId"}"""
		)
		val session = ktorClient.post("attestation/v3/preparations/sessions") {
			bearerAuth(bearerToken)
			setBody(
				CreateAttestationPreparationSessionRequest(
					platform = PLATFORM_ANDROID,
					preparationCode = preparationCode,
					keyId = keyId
				)
			)
		}.body<CreateAttestationSessionResponse>()
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

		runCatching {
			ktorClient.post("attestation/v3/preparations/complete") {
				bearerAuth(bearerToken)
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
		}.getOrElse { throwable ->
			if (shouldRecoverFromForbidden(session, throwable)) {
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
	): com.gdavidpb.tuindice.platform.android.model.ProviderAttestation {
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
	): com.gdavidpb.tuindice.base.data.model.AttestationProofOfPossessionRequest? {
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

	private fun shouldRecoverFromForbidden(
		session: CreateAttestationSessionResponse,
		throwable: Throwable
	): Boolean {
		return session.proofOfPossessionMode != null && throwable.isForbidden()
	}

	private companion object {
		const val PLATFORM_ANDROID = "Android"
	}
}

private class RecoverableAndroidKeystoreException(
	message: String,
	cause: Throwable? = null
) : RuntimeException(message, cause)
