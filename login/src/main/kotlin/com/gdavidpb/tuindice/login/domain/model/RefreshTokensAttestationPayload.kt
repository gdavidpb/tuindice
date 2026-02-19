package com.gdavidpb.tuindice.login.domain.model

import com.gdavidpb.tuindice.base.domain.model.AttestationPayload
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("refresh_token_attestation")
data class RefreshTokensAttestationPayload(
	@SerialName("access_token") val accessToken: String,
	@SerialName("refresh_token") val refreshToken: String
) : AttestationPayload()