package com.gdavidpb.tuindice.base.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IssueAttestationTokenResponse(
	@SerialName("token") val token: String,
	@SerialName("expires_at") val expiresAt: Long
)
