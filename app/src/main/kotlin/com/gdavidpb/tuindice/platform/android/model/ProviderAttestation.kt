package com.gdavidpb.tuindice.platform.android.model

import com.gdavidpb.tuindice.security.domain.model.AttestationProvider

data class ProviderAttestation(
	val token: String,
	val provider: AttestationProvider,
	val keyId: String? = null
)
