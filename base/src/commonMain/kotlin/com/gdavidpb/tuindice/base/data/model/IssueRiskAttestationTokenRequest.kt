package com.gdavidpb.tuindice.base.data.model

import com.gdavidpb.tuindice.base.domain.model.RiskAttestationEvidenceMode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IssueRiskAttestationTokenRequest(
	@SerialName("session_id") val sessionId: String,
	@SerialName("operation_code") val operationCode: String,
	@SerialName("request_hash") val requestHash: String,
	@SerialName("evidence_mode") val evidenceMode: RiskAttestationEvidenceMode,
	@SerialName("token") val token: String,
	@SerialName("key_id") val keyId: String? = null
)
