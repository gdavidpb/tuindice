package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AttemptGradingMode {
	@SerialName("numeric")
	NUMERIC,

	@SerialName("qualitative_pass_fail")
	QUALITATIVE_PASS_FAIL
}
