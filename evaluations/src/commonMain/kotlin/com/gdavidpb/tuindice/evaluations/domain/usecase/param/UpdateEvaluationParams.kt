package com.gdavidpb.tuindice.evaluations.domain.usecase.param

import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.EvaluationType

data class UpdateEvaluationParams(
	val evaluationId: String,
	val subjectId: String?,
	val subjectCode: String?,
	val quarterId: String?,
	val scheduleMode: EvaluationScheduleMode?,
	val grade: Double?,
	val maxGrade: Double?,
	val date: Long?,
	val type: EvaluationType?
)
