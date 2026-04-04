package com.gdavidpb.tuindice.base.domain.model

data class AttestationRequest(
    val operationCode: ProtectedOperationCode,
    val payloadJson: String,
    val bearerToken: String
)
