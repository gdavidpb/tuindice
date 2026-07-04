package com.gdavidpb.tuindice.evaluations.data.model

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode

data class LocalEvaluation(
	val id: String,
	val referenceId: String,
	val attemptId: String,
	val subjectCode: String,
	val termId: String,
	val revision: Long,
	val scheduleMode: EvaluationScheduleMode,
	val grade: Double?,
	val maxGrade: Double,
	val date: Long?,
	val type: Int,
	val isDone: Boolean
)
