package com.gdavidpb.tuindice.evaluations.data.model

import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EvaluationResponse(
	@SerialName("id") val id: String,
	@SerialName("reference_id") val referenceId: String,
	@SerialName("quarter_id") val quarterId: String,
	@SerialName("subject_id") val subjectId: String,
	@SerialName("subject_code") val subjectCode: String,
	@SerialName("revision") val revision: Long,
	@SerialName("type") val type: Int,
	@SerialName("schedule_mode") val scheduleMode: EvaluationScheduleMode,
	@SerialName("grade") val grade: Double?,
	@SerialName("max_grade") val maxGrade: Double,
	@SerialName("date") val date: Long?,
	@SerialName("is_done") val isDone: Boolean
)
