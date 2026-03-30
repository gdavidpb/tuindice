package com.gdavidpb.tuindice.data.source.attestation

import com.gdavidpb.tuindice.base.domain.model.AttestationEvidenceMode
import com.gdavidpb.tuindice.platform.android.model.ProviderAttestation

interface AttestationProviderDataSource {
	suspend fun getAttestation(
		bindingHash: String,
		evidenceMode: AttestationEvidenceMode
	): ProviderAttestation?
}
