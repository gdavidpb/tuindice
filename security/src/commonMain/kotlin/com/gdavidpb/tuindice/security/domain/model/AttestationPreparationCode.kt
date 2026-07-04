package com.gdavidpb.tuindice.security.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AttestationPreparationCode(val value: String) {
	@SerialName("bootstrap")
	BOOTSTRAP("bootstrap")
}
