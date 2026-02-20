package com.gdavidpb.tuindice.evaluations.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EvaluationResponse(
	@SerialName("id") val id: String,
	@SerialName("quarter_id") val quarterId: String,
	@SerialName("subject_id") val subjectId: String,
	@SerialName("subject_code") val subjectCode: String,
	@SerialName("type") val type: Int,
	@SerialName("grade") val grade: Double?,
	@SerialName("max_grade") val maxGrade: Double,
	@SerialName("date") val date: Long?,
	@SerialName("is_done") val isDone: Boolean,
	@SerialName("notes") val notes: String
)