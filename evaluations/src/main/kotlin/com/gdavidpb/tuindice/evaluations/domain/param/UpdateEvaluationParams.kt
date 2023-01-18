package com.gdavidpb.tuindice.evaluations.domain.param

import com.gdavidpb.tuindice.base.domain.model.EvaluationType

data class UpdateEvaluationParams(
	val evaluationId: String,
	val name: String?,
	val grade: Double?,
	val maxGrade: Double?,
	val date: Long?,
	val type: EvaluationType?,
	val isDone: Boolean?
)