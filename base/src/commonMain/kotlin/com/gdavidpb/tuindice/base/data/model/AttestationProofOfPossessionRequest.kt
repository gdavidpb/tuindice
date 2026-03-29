package com.gdavidpb.tuindice.base.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttestationProofOfPossessionRequest(
	@SerialName("signature") val signature: String,
	@SerialName("public_key") val publicKey: String
)
