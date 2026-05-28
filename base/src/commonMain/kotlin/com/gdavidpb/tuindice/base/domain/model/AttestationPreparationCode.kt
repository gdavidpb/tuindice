package com.gdavidpb.tuindice.base.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AttestationPreparationCode(val value: String) {
	@SerialName("bootstrap")
	BOOTSTRAP("bootstrap")
}
