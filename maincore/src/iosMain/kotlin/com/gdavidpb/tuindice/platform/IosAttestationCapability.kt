package com.gdavidpb.tuindice.platform

import com.gdavidpb.tuindice.domain.model.IosPlatformAttestation

interface IosAttestationCapability {
	fun sha256Base64Url(value: String): String?
	suspend fun resolveAttestationKeyId(): String?
	suspend fun invalidateAttestationKeyId()
	suspend fun requestAttestation(
		attestationInput: String,
		keyId: String,
		evidenceMode: String
	): IosPlatformAttestation?
}
