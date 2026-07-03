package com.gdavidpb.tuindice.security.data.model

import com.gdavidpb.tuindice.security.domain.model.AttestationEvidenceMode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IssueAttestationTokenRequest(
	@SerialName("session_id") val sessionId: String,
	@SerialName("operation_code") val operationCode: String,
	@SerialName("request_hash") val requestHash: String,
	@SerialName("evidence_mode") val evidenceMode: AttestationEvidenceMode,
	@SerialName("token") val token: String,
	@SerialName("key_id") val keyId: String,
	@SerialName("proof_of_possession") val proofOfPossession: AttestationProofOfPossessionRequest? = null
)
