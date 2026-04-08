package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AttemptScoreKind {
	@SerialName("numeric")
	NUMERIC,

	@SerialName("symbolic")
	SYMBOLIC,

	@SerialName("empty")
	EMPTY
}
