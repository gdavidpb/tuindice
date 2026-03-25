package com.gdavidpb.tuindice.evaluations.data.model

import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode

data class LocalEvaluation(
	val id: String,
	val referenceId: String,
	val subjectId: String,
	val subjectCode: String,
	val quarterId: String,
	val revision: Long,
	val scheduleMode: EvaluationScheduleMode,
	val grade: Double?,
	val maxGrade: Double,
	val date: Long?,
	val type: Int,
	val isDone: Boolean
)
