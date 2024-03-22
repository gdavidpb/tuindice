package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddEvaluationRequest(
	@SerialName("reference") val reference: String,
	@SerialName("subject_id") val subjectId: String,
	@SerialName("type") val type: Int,
	@SerialName("grade") val grade: Double? = null,
	@SerialName("max_grade") val maxGrade: Double,
	@SerialName("date") val date: Long?
)