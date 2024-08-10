package com.gdavidpb.tuindice.data.repository.attestation.source

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.data.repository.attestation.ProviderDataSource
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.google.android.play.core.integrity.StandardIntegrityManager.PrepareIntegrityTokenRequest
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenRequest
import kotlinx.coroutines.tasks.await

class PlayIntegrityDataSource(
	private val standardIntegrityManager: StandardIntegrityManager
) : ProviderDataSource {
	override suspend fun getToken(nonce: String): String? {
		val prepareRequest = PrepareIntegrityTokenRequest
			.builder()
			.setCloudProjectNumber(BuildConfig.GOOGLE_CLOUD_PROJECT_NUMBER)
			.build()

		val provider = standardIntegrityManager
			.prepareIntegrityToken(prepareRequest)
			.await()

		val integrityRequest = StandardIntegrityTokenRequest
			.builder()
			.setRequestHash(nonce)
			.build()

		return provider
			.request(integrityRequest)
			.await()
			.token()
	}
}