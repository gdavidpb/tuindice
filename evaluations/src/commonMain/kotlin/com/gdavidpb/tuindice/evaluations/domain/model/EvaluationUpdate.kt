package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType

data class EvaluationUpdate(
	val id: String,
	val scheduleMode: EvaluationScheduleMode?,
	val grade: Double?,
	val maxGrade: Double?,
	val date: Long?,
	val type: EvaluationType?
)
