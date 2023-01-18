package com.gdavidpb.tuindice.evaluations.domain.param

import com.gdavidpb.tuindice.base.domain.model.EvaluationType

data class AddEvaluationParams(
	val subjectId: String,
	val name: String?,
	val grade: Double?,
	val maxGrade: Double?,
	val date: Long?,
	val type: EvaluationType?,
	val isDone: Boolean?
)