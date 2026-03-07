package com.gdavidpb.tuindice.base.domain.model

data class RiskAttestationRequest(
    val operation: RiskOperation,
    val payloadJson: String
)
