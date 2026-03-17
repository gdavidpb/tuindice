package com.gdavidpb.tuindice.base.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateRiskAttestationSessionRequest(
	@SerialName("platform") val platform: String,
	@SerialName("key_id") val keyId: String? = null
)
