package com.gdavidpb.tuindice.evaluations.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateEvaluationRequest(
	@SerialName("evaluation_id") val evaluationId: String,
	@SerialName("grade") val grade: Double? = null,
	@SerialName("max_grade") val maxGrade: Double? = null,
	@SerialName("date") val date: Long? = null,
	@SerialName("type") val type: Int? = null,
	@SerialName("is_done") val isDone: Boolean? = null
)