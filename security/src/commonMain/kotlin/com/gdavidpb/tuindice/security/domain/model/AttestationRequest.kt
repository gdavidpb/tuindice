package com.gdavidpb.tuindice.security.domain.model

data class AttestationRequest(
    val operationCode: ProtectedOperationCode,
    val payloadJson: String,
    val authorization: AttestationAuthorization
)
