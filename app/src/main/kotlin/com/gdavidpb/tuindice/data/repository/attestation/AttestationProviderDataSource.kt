package com.gdavidpb.tuindice.data.repository.attestation

import com.gdavidpb.tuindice.platform.android.model.ProviderAttestation

interface AttestationProviderDataSource {
	suspend fun getAttestation(nonce: String): ProviderAttestation?
}
