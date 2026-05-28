package com.gdavidpb.tuindice.base.domain.model

class AttestationTemporarilyUnavailableException(
    val platform: String,
    val operationCode: String,
    cause: Throwable? = null
) : IllegalStateException(
    "Attestation temporarily unavailable for $platform operation $operationCode.",
    cause
)
