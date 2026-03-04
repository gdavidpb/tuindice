package com.gdavidpb.tuindice.data

import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.data.repository.attestation.AttestationProviderDataSource
import com.gdavidpb.tuindice.platform.android.model.ProviderAttestation
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class MockAttestationProviderDataSource : AttestationProviderDataSource {
	override suspend fun getAttestation(nonce: String): ProviderAttestation {
		return ProviderAttestation(
			token = Uuid.random().toString(),
			provider = AttestationProvider.PLAY_INTEGRITY
		)
	}
}
