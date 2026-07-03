package com.gdavidpb.tuindice.evaluations.domain.usecase.param

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType

data class AddEvaluationParams(
	val attemptId: String?,
	val subjectCode: String?,
	val termId: String?,
	val type: EvaluationType?,
	val scheduleMode: EvaluationScheduleMode,
	val date: Long?,
	val grade: Double?,
	val maxGrade: Double?
)
