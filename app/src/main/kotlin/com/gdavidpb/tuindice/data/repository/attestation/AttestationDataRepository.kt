package com.gdavidpb.tuindice.data.repository.attestation

import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository

class AttestationDataRepository(
	private val remoteDataSource: RemoteDataSource,
	private val providerDataSource: ProviderDataSource,
	private val digestDataSource: DigestDataSource
) : AttestationRepository {
	override suspend fun getAttestation(payload: String): Attestation {
		val (id, challenge) = remoteDataSource.getChallenge()
		val nonce = digestDataSource.digest(challenge, payload)
		val token = providerDataSource.getToken(nonce)

		requireNotNull(token)

		return Attestation(
			id = id,
			token = token
		)
	}
}