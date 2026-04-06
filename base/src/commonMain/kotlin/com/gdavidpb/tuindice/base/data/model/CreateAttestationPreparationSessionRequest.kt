package com.gdavidpb.tuindice.base.data.model

import com.gdavidpb.tuindice.base.domain.model.AttestationPreparationCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateAttestationPreparationSessionRequest(
	@SerialName("platform") val platform: String,
	@SerialName("preparation_code") val preparationCode: AttestationPreparationCode,
	@SerialName("authorization") val authorization: AttestationAuthorizationRequest? = null,
	@SerialName("key_id") val keyId: String
)
