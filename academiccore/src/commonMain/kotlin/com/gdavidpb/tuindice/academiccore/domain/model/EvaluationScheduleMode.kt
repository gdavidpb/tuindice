package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class EvaluationScheduleMode {
	@SerialName("dated")
	DATED,

	@SerialName("continuous")
	CONTINUOUS
}
