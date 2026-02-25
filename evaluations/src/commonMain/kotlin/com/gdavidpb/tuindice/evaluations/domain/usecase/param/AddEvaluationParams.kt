package com.gdavidpb.tuindice.evaluations.domain.usecase.param

import com.gdavidpb.tuindice.base.domain.model.EvaluationType

data class AddEvaluationParams(
	val subjectId: String?,
	val subjectCode: String?,
	val quarterId: String?,
	val type: EvaluationType?,
	val date: Long?,
	val grade: Double?,
	val maxGrade: Double?
)