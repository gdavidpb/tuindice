package com.gdavidpb.tuindice.evaluations.domain.param

import com.gdavidpb.tuindice.base.domain.model.EvaluationType

data class UpdateEvaluationParams(
	val evaluationId: String,
	val name: String? = null,
	val grade: Double? = null,
	val maxGrade: Double? = null,
	val date: Long? = null,
	val type: EvaluationType? = null,
	val isDone: Boolean? = null
)