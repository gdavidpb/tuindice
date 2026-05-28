package com.gdavidpb.tuindice.subjects.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SubjectDifficultyBand {
	@SerialName("low")
	LOW,

	@SerialName("medium")
	MEDIUM,

	@SerialName("high")
	HIGH,

	@SerialName("very_high")
	VERY_HIGH
}
