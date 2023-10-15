package com.gdavidpb.tuindice.evaluations.domain.usecase.param

import com.gdavidpb.tuindice.base.domain.model.EvaluationType

data class AddEvaluationParams(
	val quarterId: String?,
	val subjectId: String?,
	val maxGrade: Double?,
	val date: Long?,
	val type: EvaluationType?
)