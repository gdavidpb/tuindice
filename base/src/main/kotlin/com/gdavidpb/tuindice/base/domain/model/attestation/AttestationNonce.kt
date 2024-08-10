package com.gdavidpb.tuindice.base.domain.model.attestation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttestationNonce(
	@SerialName("payload") val payload: AttestationPayload
)