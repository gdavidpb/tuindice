package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.base.domain.model.EvaluationType

data class UpdateEvaluation(
	val evaluationId: String,
	val name: String?,
	val grade: Double?,
	val maxGrade: Double?,
	val date: Long?,
	val type: EvaluationType?,
	val isDone: Boolean?
)