package com.gdavidpb.tuindice.security.data.model

import com.gdavidpb.tuindice.security.domain.model.AttestationEvidenceMode
import com.gdavidpb.tuindice.security.domain.model.AttestationPreparationCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CompleteAttestationPreparationRequest(
	@SerialName("session_id") val sessionId: String,
	@SerialName("preparation_code") val preparationCode: AttestationPreparationCode,
	@SerialName("request_hash") val requestHash: String,
	@SerialName("evidence_mode") val evidenceMode: AttestationEvidenceMode,
	@SerialName("token") val token: String,
	@SerialName("key_id") val keyId: String,
	@SerialName("proof_of_possession") val proofOfPossession: AttestationProofOfPossessionRequest? = null
)
