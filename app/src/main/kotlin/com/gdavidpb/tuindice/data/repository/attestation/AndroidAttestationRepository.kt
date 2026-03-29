package com.gdavidpb.tuindice.data.repository.attestation

import com.gdavidpb.tuindice.base.data.model.CreateAttestationSessionRequest
import com.gdavidpb.tuindice.base.data.model.CreateAttestationSessionResponse
import com.gdavidpb.tuindice.base.data.model.IssueAttestationTokenRequest
import com.gdavidpb.tuindice.base.data.model.IssueAttestationTokenResponse
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationRequest
import com.gdavidpb.tuindice.base.domain.model.AttestedOperation
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.utils.attestationBindingInput
import com.gdavidpb.tuindice.base.utils.extension.isForbidden
import com.gdavidpb.tuindice.platform.android.AndroidProofOfPossessionCapability
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import java.security.MessageDigest
import kotlin.io.encoding.Base64
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AndroidAttestationRepository(
	private val ktorClient: HttpClient,
	private val providerDataSource: AttestationProviderDataSource,
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
		val keyId = resolveKeyIdIfNeeded(request.operation)
		val session = ktorClient.post("attestation/v2/sessions") {
			setBody(
				CreateAttestationSessionRequest(
					platform = PLATFORM_ANDROID,
					operationCode = request.operation.code,
					keyId = keyId
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
		val proofOfPossession = session.proofOfPossessionMode?.let {
			val requiredKeyId = keyId?.takeIf { value -> value.isNotBlank() }
				?: throw RecoverableAndroidKeystoreException("Android proof-of-possession keyId unavailable.")

			runCatching {
				proofOfPossessionCapability.createProofOfPossession(
					attestationInput = bindingHash,
					keyId = requiredKeyId
				)
			}.getOrElse { throwable ->
				throw RecoverableAndroidKeystoreException(
					message = "Unable to produce Android proof-of-possession evidence.",
					cause = throwable
				)
			}
		}

		val response = runCatching {
			ktorClient.post("attestation/v2/tokens") {
				setBody(
					IssueAttestationTokenRequest(
						sessionId = session.sessionId,
						operationCode = request.operation.code,
						requestHash = requestHash,
						evidenceMode = session.evidenceMode,
						token = providerAttestation.token,
						keyId = keyId,
						proofOfPossession = proofOfPossession
					)
				)
			}.body<IssueAttestationTokenResponse>()
		}.getOrElse { throwable ->
			if (session.proofOfPossessionMode != null && throwable.isForbidden()) {
				throw RecoverableAndroidKeystoreException(
					message = "Android proof-of-possession evidence rejected by backend.",
					cause = throwable
				)
			}

			throw throwable
		}

		return Attestation(token = response.token)
	}

	private suspend fun resolveKeyIdIfNeeded(operation: AttestedOperation): String? {
		if (operation != AttestedOperation.RefreshTokens) {
			return null
		}

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

	private companion object {
		const val PLATFORM_ANDROID = "Android"
	}
}

private class RecoverableAndroidKeystoreException(
	message: String,
	cause: Throwable? = null
) : RuntimeException(message, cause)
