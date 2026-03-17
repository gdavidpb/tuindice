package com.gdavidpb.tuindice.base.data.model

import com.gdavidpb.tuindice.base.domain.model.RiskAttestationEvidenceMode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateRiskAttestationSessionResponse(
	@SerialName("session_id") val sessionId: String,
	@SerialName("challenge") val challenge: String,
	@SerialName("expires_at") val expiresAt: Long,
	@SerialName("evidence_mode") val evidenceMode: RiskAttestationEvidenceMode
)
