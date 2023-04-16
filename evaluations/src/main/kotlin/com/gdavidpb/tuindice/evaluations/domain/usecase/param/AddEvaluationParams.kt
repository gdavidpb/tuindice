package com.gdavidpb.tuindice.evaluations.domain.usecase.param

import com.gdavidpb.tuindice.base.domain.model.EvaluationType

data class AddEvaluationParams(
	val quarterId: String,
	val subjectId: String,
	val subjectCode: String,
	val name: String?,
	val grade: Double?,
	val maxGrade: Double?,
	val date: Long?,
	val type: EvaluationType?,
	val isDone: Boolean?
)