package com.gdavidpb.tuindice.data.repository.attestation

import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationPayload
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository

class AttestationDataRepository(
	private val remoteDataSource: RemoteDataSource,
	private val providerDataSource: ProviderDataSource,
	private val payloadDigestDataSource: PayloadDigestDataSource
) : AttestationRepository {
	override suspend fun getAttestation(payload: AttestationPayload): Attestation {
		val (id, challenge) = remoteDataSource.getChallenge()
		val nonce = payloadDigestDataSource.digest(challenge, payload)
		val token = providerDataSource.getToken(nonce)

		requireNotNull(token)

		return Attestation(
			id = id,
			token = token
		)
	}
}