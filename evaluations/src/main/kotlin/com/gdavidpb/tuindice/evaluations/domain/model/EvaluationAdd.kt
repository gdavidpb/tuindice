package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.base.domain.model.EvaluationType

data class EvaluationAdd(
	val reference: String,
	val quarterId: String,
	val subjectId: String,
	val type: EvaluationType,
	val date: Long,
	val grade: Double,
	val maxGrade: Double
)
