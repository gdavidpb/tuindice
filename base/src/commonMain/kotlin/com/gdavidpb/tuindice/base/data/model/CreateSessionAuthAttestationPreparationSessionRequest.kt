package com.gdavidpb.tuindice.base.data.model

import com.gdavidpb.tuindice.base.domain.model.AttestationPreparationCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateSessionAuthAttestationPreparationSessionRequest(
    @SerialName("auth_session_id") val authSessionId: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("platform") val platform: String,
    @SerialName("preparation_code") val preparationCode: AttestationPreparationCode,
    @SerialName("key_id") val keyId: String? = null
)
