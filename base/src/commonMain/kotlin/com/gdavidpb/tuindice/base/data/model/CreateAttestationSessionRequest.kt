package com.gdavidpb.tuindice.base.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateAttestationSessionRequest(
	@SerialName("platform") val platform: String,
	@SerialName("operation_code") val operationCode: String,
	@SerialName("authorization") val authorization: AttestationAuthorizationRequest? = null,
	@SerialName("key_id") val keyId: String
)
