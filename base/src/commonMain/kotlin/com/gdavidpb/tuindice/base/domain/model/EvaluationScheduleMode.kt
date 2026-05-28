package com.gdavidpb.tuindice.base.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class EvaluationScheduleMode {
	@SerialName("dated")
	DATED,

	@SerialName("continuous")
	CONTINUOUS
}
