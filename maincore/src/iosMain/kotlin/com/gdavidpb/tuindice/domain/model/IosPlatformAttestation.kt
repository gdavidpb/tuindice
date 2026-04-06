package com.gdavidpb.tuindice.domain.model

import com.gdavidpb.tuindice.base.domain.model.AttestationProvider

data class IosPlatformAttestation(
	val token: String,
	val keyId: String,
	val provider: AttestationProvider = AttestationProvider.APP_ATTEST
)
