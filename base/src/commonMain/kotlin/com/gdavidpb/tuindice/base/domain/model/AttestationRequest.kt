package com.gdavidpb.tuindice.base.domain.model

data class AttestationRequest(
    val operationCode: ProtectedOperationCode,
    val payloadJson: String,
    val authorization: AttestationAuthorization
)

sealed interface AttestationAuthorization {
    data class Bearer(
        val accessToken: String
    ) : AttestationAuthorization

    data class Session(
        val sessionId: String,
        val refreshToken: String
    ) : AttestationAuthorization
}
