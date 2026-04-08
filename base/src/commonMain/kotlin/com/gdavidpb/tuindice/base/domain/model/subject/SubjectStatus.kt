package com.gdavidpb.tuindice.base.domain.model.subject

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SubjectStatus(val value: String) {
	@SerialName("normal")
	NORMAL("normal"),

	@SerialName("approved")
	APPROVED("approved"),

	@SerialName("failed")
	FAILED("failed"),

	@SerialName("retired")
	RETIRED("retired"),

	@SerialName("without_effect")
	WITHOUT_EFFECT("without_effect")
}
