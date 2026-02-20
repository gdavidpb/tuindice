package com.gdavidpb.tuindice.evaluations.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddEvaluationRequest(
	@SerialName("subject_id") val subjectId: String,
	@SerialName("grade") val grade: Double?,
	@SerialName("max_grade") val maxGrade: Double,
	@SerialName("date") val date: Long?,
	@SerialName("type") val type: Int,
	@SerialName("is_done") val isDone: Boolean
)