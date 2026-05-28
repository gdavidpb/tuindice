package com.gdavidpb.tuindice.base.data.model

import com.gdavidpb.tuindice.base.domain.model.AttestationEvidenceMode
import com.gdavidpb.tuindice.base.domain.model.AttestationProofOfPossessionMode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateAttestationSessionResponse(
	@SerialName("session_id") val sessionId: String,
	@SerialName("challenge") val challenge: String,
	@SerialName("expires_at") val expiresAt: Long,
	@SerialName("evidence_mode") val evidenceMode: AttestationEvidenceMode,
	@SerialName("proof_of_possession_mode") val proofOfPossessionMode: AttestationProofOfPossessionMode? = null
)
