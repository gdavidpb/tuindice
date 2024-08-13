package com.gdavidpb.tuindice.data.repository.attestation

import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository

class AttestationDataRepository(
	private val localDataSource: LocalDataSource,
	private val providerDataSource: ProviderDataSource
) : AttestationRepository {
	override suspend fun getToken(payload: String): String {
		val nonce = localDataSource.getNonce(payload)
		val token = providerDataSource.getToken(nonce)

		requireNotNull(token)

		return token
	}
}