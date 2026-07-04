package com.gdavidpb.tuindice.security.data.model

import com.gdavidpb.tuindice.security.domain.model.AttestationPreparationCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttestationPreparationRequiredResponse(
	@SerialName("code") val code: String,
	@SerialName("required_preparation_code") val requiredPreparationCode: AttestationPreparationCode
)
