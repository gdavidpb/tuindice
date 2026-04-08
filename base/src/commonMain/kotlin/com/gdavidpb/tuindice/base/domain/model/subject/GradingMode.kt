package com.gdavidpb.tuindice.base.domain.model.subject

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class GradingMode(val value: String) {
	@SerialName("numeric")
	NUMERIC("numeric"),

	@SerialName("qualitative_pass_fail")
	QUALITATIVE_PASS_FAIL("qualitative_pass_fail")
}
