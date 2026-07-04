package com.gdavidpb.tuindice.data.repository.attestation

import com.gdavidpb.tuindice.platform.android.model.ProviderAttestation
import com.gdavidpb.tuindice.security.domain.model.AttestationEvidenceMode

interface AttestationProviderDataRepository {
	suspend fun getAttestation(
		bindingHash: String,
		evidenceMode: AttestationEvidenceMode
	): ProviderAttestation?
}
