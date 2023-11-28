package com.gdavidpb.tuindice.data.repository.attestation

import com.gdavidpb.tuindice.base.domain.model.attestation.AttestationPayload
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository

class AttestationDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource,
	private val providerDataSource: ProviderDataSource
) : AttestationRepository {
	override suspend fun getToken(operation: String, payload: AttestationPayload): String {
		val identifier = remoteDataSource.getAttestationId(operation)
		val nonce = localDataSource.getNonce(identifier, payload)

		return providerDataSource.getToken(nonce)
	}
}