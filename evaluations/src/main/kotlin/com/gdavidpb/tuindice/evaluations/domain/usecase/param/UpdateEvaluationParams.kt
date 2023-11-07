package com.gdavidpb.tuindice.evaluations.domain.usecase.param

import com.gdavidpb.tuindice.base.domain.model.EvaluationType

data class UpdateEvaluationParams(
	val evaluationId: String,
	val subjectId: String? = null,
	val grade: Double? = null,
	val maxGrade: Double? = null,
	val date: Long? = null,
	val type: EvaluationType? = null,
	val isCompleted: Boolean? = null
)