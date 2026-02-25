package com.gdavidpb.tuindice.data

import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.data.repository.attestation.ProviderDataSource
import com.gdavidpb.tuindice.data.repository.attestation.model.ProviderAttestation
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class MockAttestationProviderDataSource : ProviderDataSource {
	override suspend fun getAttestation(nonce: String): ProviderAttestation {
		return ProviderAttestation(
			token = Uuid.random().toString(),
			provider = AttestationProvider.PLAY_INTEGRITY
		)
	}
}
