package com.gdavidpb.tuindice.evaluations.data.model

import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode

data class RemoteEvaluation(
	val id: String,
	val subjectId: String,
	val subjectCode: String,
	val quarterId: String,
	val scheduleMode: EvaluationScheduleMode,
	val grade: Double?,
	val maxGrade: Double,
	val date: Long?,
	val type: Int,
	val isDone: Boolean
)
