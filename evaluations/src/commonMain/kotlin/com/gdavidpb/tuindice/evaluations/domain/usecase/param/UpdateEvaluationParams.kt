package com.gdavidpb.tuindice.evaluations.domain.usecase.param

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType

data class UpdateEvaluationParams(
	val evaluationId: String,
	val attemptId: String?,
	val subjectCode: String?,
	val termId: String?,
	val scheduleMode: EvaluationScheduleMode?,
	val grade: Double?,
	val maxGrade: Double?,
	val date: Long?,
	val type: EvaluationType?
)
