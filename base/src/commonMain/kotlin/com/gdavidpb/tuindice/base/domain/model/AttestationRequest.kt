package com.gdavidpb.tuindice.base.domain.model

data class AttestationRequest(
    val operation: AttestedOperation,
    val payloadJson: String
)
