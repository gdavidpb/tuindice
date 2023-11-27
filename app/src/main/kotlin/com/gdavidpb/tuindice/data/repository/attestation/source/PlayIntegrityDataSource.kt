package com.gdavidpb.tuindice.data.repository.attestation.source

import com.gdavidpb.tuindice.data.repository.attestation.ProviderDataSource
import com.google.android.play.core.integrity.IntegrityManager
import com.google.android.play.core.integrity.IntegrityTokenRequest
import kotlinx.coroutines.tasks.await

class PlayIntegrityDataSource(
	private val integrityManager: IntegrityManager
) : ProviderDataSource {
	override suspend fun getToken(nonce: String): String {
		val request = IntegrityTokenRequest
			.builder()
			.setNonce(nonce)
			.build()

		return integrityManager
			.requestIntegrityToken(request)
			.await()
			.token()
	}
}