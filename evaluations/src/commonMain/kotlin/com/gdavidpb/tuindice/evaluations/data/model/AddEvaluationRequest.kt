package com.gdavidpb.tuindice.evaluations.data.model

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddEvaluationRequest(
	@SerialName("reference_id") val referenceId: String,
	@SerialName("attempt_id") val attemptId: String,
	@SerialName("term_id") val termId: String,
	@SerialName("schedule_mode") val scheduleMode: EvaluationScheduleMode,
	@SerialName("grade") val grade: Double?,
	@SerialName("max_grade") val maxGrade: Double,
	@SerialName("date") val date: Long?,
	@SerialName("type") val type: Int,
	@SerialName("is_done") val isDone: Boolean,
	@SerialName("mutation_id") val mutationId: String
)
