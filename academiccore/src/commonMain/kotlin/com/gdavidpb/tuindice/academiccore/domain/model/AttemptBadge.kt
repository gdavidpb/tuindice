package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AttemptBadge {
	@SerialName("none")
	NONE,

	@SerialName("without_effect")
	WITHOUT_EFFECT,

	@SerialName("equivalence")
	EQUIVALENCE
}
