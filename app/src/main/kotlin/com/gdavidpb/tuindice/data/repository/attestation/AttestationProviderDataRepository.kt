package com.gdavidpb.tuindice.data.repository.attestation

import com.gdavidpb.tuindice.security.domain.model.AttestationEvidenceMode
import com.gdavidpb.tuindice.platform.android.model.ProviderAttestation

interface AttestationProviderDataRepository {
	suspend fun getAttestation(
		bindingHash: String,
		evidenceMode: AttestationEvidenceMode
	): ProviderAttestation?
}
