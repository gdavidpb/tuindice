package com.gdavidpb.tuindice.data.source.attestation

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.base.domain.model.AttestationEvidenceMode
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.data.contract.attestation.AttestationProviderDataSource
import com.gdavidpb.tuindice.platform.android.model.ProviderAttestation
import com.google.android.play.core.integrity.IntegrityManager
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.android.play.core.integrity.StandardIntegrityManager
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PlayIntegrityDataSource(
	private val integrityManager: IntegrityManager,
	private val standardIntegrityManager: StandardIntegrityManager
) : AttestationProviderDataSource {
	private val standardTokenProviderMutex = Mutex()
	@Volatile
	private var standardTokenProvider: StandardIntegrityManager.StandardIntegrityTokenProvider? = null

	override suspend fun getAttestation(
		bindingHash: String,
		evidenceMode: AttestationEvidenceMode
	): ProviderAttestation {
		val token = when (evidenceMode) {
			AttestationEvidenceMode.PLAY_INTEGRITY_STANDARD -> requestStandardAttestation(bindingHash)
			AttestationEvidenceMode.PLAY_INTEGRITY_CLASSIC -> requestClassicAttestation(bindingHash)
			AttestationEvidenceMode.APP_ATTEST_ATTESTATION,
			AttestationEvidenceMode.APP_ATTEST_ASSERTION ->
				throw IllegalArgumentException("Unsupported Android attestation evidence mode: $evidenceMode.")
		}

		return ProviderAttestation(
			token = token,
			provider = AttestationProvider.PLAY_INTEGRITY
		)
	}

	private suspend fun requestClassicAttestation(bindingHash: String): String {
		val request = IntegrityTokenRequest.builder()
			.setNonce(bindingHash)
			.build()

		val response = integrityManager
			.requestIntegrityToken(request)
			.await()

		return response.token()
	}

	private suspend fun requestStandardAttestation(bindingHash: String): String {
		return runCatching {
			requestPreparedStandardToken(bindingHash)
		}.recoverCatching {
			delay(STANDARD_RETRY_DELAY_MS)
			standardTokenProvider = null
			requestPreparedStandardToken(bindingHash)
		}.getOrThrow()
	}

	private suspend fun requestPreparedStandardToken(bindingHash: String): String {
		val request = StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
			.setRequestHash(bindingHash)
			.build()

		return standardTokenProvider()
			.request(request)
			.await()
			.token()
	}

	private suspend fun standardTokenProvider(): StandardIntegrityManager.StandardIntegrityTokenProvider {
		standardTokenProvider?.let { return it }

		return standardTokenProviderMutex.withLock {
			standardTokenProvider ?: standardIntegrityManager
				.prepareIntegrityToken(
					StandardIntegrityManager.PrepareIntegrityTokenRequest.builder()
						.setCloudProjectNumber(BuildConfig.PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER)
						.build()
				)
				.await()
				.also { provider -> standardTokenProvider = provider }
		}
	}

	private companion object {
		const val STANDARD_RETRY_DELAY_MS = 250L
	}
}
