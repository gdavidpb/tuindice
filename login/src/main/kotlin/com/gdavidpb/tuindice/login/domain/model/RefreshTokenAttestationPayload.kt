package com.gdavidpb.tuindice.login.domain.model

import com.gdavidpb.tuindice.base.domain.model.AttestationPayload
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenAttestationPayload(
	@SerialName("access_token") val accessToken: String,
	@SerialName("refresh_token") val refreshToken: String
) : AttestationPayload()