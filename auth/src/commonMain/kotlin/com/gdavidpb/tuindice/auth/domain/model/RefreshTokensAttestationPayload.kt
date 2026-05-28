package com.gdavidpb.tuindice.auth.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokensAttestationPayload(
    @SerialName("session_id") val sessionId: String,
    @SerialName("refresh_token") val refreshToken: String
)
