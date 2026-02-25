package com.gdavidpb.tuindice.data.repository.attestation.model

import com.gdavidpb.tuindice.base.domain.model.AttestationProvider

data class ProviderAttestation(
	val token: String,
	val provider: AttestationProvider,
	val keyId: String? = null
)
