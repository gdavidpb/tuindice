package com.gdavidpb.tuindice.base.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateSessionAuthAttestationSessionRequest(
    @SerialName("auth_session_id") val authSessionId: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("platform") val platform: String,
    @SerialName("operation_code") val operationCode: String,
    @SerialName("key_id") val keyId: String? = null
)
