package com.gdavidpb.tuindice.data.repository.attestation

import com.gdavidpb.tuindice.data.repository.attestation.model.ProviderAttestation

interface ProviderDataSource {
	suspend fun getAttestation(nonce: String): ProviderAttestation?
}
