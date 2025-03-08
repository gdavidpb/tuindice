package com.gdavidpb.tuindice.data.repository.attestation.source.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChallengeResponse(
	@SerialName("id") val id: String,
	@SerialName("challenge") val challenge: String
)
