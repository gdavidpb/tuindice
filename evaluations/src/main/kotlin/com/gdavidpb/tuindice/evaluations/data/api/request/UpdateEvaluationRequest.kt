package com.gdavidpb.tuindice.evaluations.data.api.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateEvaluationRequest(
	@SerialName("evaluation_id") val evaluationId: String,
	@SerialName("grade") val grade: Double?,
	@SerialName("max_grade") val maxGrade: Double?,
	@SerialName("date") val date: Long?,
	@SerialName("type") val type: Int?
)