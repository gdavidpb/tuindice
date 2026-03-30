package com.gdavidpb.tuindice.platform.android

import com.gdavidpb.tuindice.base.data.model.AttestationProofOfPossessionRequest

interface AndroidProofOfPossessionCapability {
	suspend fun resolveProofOfPossessionKeyId(): String
	suspend fun invalidateProofOfPossessionKeyId()
	suspend fun createProofOfPossession(
		attestationInput: String,
		keyId: String,
		requireKeyAttestation: Boolean
	): AttestationProofOfPossessionRequest
}
