package com.gdavidpb.tuindice.academiccore.domain.model

data class Evaluation(
	val id: String,
	val attemptId: String,
	val subjectCode: String,
	val termId: String,
	val scheduleMode: EvaluationScheduleMode,
	val grade: Double?,
	val maxGrade: Double,
	val date: Long?,
	val type: EvaluationType,
	val state: EvaluationState
)
