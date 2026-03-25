package com.gdavidpb.tuindice.data.repository.attestation.source

import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.data.repository.attestation.AttestationProviderDataSource
import com.gdavidpb.tuindice.platform.android.model.ProviderAttestation
import com.google.android.play.core.integrity.IntegrityManager
import com.google.android.play.core.integrity.IntegrityTokenRequest
import kotlinx.coroutines.tasks.await

class PlayIntegrityDataSource(
	private val integrityManager: IntegrityManager
) : AttestationProviderDataSource {
	override suspend fun getAttestation(nonce: String): ProviderAttestation {
		val request = IntegrityTokenRequest.builder()
			.setNonce(nonce)
			.build()

		val response = integrityManager
			.requestIntegrityToken(request)
			.await()

		return ProviderAttestation(
			token = response.token(),
			provider = AttestationProvider.PLAY_INTEGRITY
		)
	}
}
