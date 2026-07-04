package com.gdavidpb.tuindice.security.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttestationProofOfPossessionRequest(
	@SerialName("signature") val signature: String,
	@SerialName("public_key") val publicKey: String,
	@SerialName("attestation_certificate_chain") val attestationCertificateChain: List<String>? = null
)
