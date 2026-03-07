package com.gdavidpb.tuindice.data

import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.logging.appLogger
import com.gdavidpb.tuindice.data.repository.attestation.AttestationProviderDataSource
import com.gdavidpb.tuindice.platform.android.model.ProviderAttestation

class MockAttestationProviderDataSource : AttestationProviderDataSource {
	private val logger = appLogger(tag = "Attestation")

	override suspend fun getAttestation(nonce: String): ProviderAttestation {
		logger.i { "[android-debug] getAttestation(): returning debug Play Integrity evidence for mocked API." }

		return ProviderAttestation(
			token = "$ANDROID_DEBUG_ATTESTATION_PREFIX$nonce",
			provider = AttestationProvider.PLAY_INTEGRITY
		)
	}
}

private const val ANDROID_DEBUG_ATTESTATION_PREFIX = "android-debug-attestation:"
