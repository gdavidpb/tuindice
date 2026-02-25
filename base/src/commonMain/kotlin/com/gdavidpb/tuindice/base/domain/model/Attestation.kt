package com.gdavidpb.tuindice.base.domain.model

data class Attestation(
	val id: String,
	val token: String,
	val provider: AttestationProvider,
	val keyId: String? = null
)
