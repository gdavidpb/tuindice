package com.gdavidpb.tuindice.base.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateRiskAttestationSessionResponse(
	@SerialName("session_id") val sessionId: String,
	@SerialName("challenge") val challenge: String,
	@SerialName("expires_at") val expiresAt: Long
)
