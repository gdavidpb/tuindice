package com.gdavidpb.tuindice.data.repository.attestation

import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationPayload
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository

class AttestationDataRepository(
	private val remoteDataSource: AttestationRemoteDataSource,
	private val providerDataSource: AttestationProviderDataSource,
	private val payloadDigestDataSource: PayloadDigestDataSource
) : AttestationRepository {
	override suspend fun getAttestation(payload: AttestationPayload): Attestation {
		val (id, challenge) = remoteDataSource.getChallenge()
		val nonce = payloadDigestDataSource.digest(challenge, payload)
		val providerAttestation = providerDataSource.getAttestation(nonce)

		requireNotNull(providerAttestation)

		return Attestation(
			id = id,
			token = providerAttestation.token,
			provider = providerAttestation.provider,
			keyId = providerAttestation.keyId
		)
	}
}
