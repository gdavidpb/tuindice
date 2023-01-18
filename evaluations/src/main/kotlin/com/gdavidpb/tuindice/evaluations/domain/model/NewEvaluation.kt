package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.base.domain.model.EvaluationType

data class NewEvaluation(
	val subjectId: String,
	val name: String,
	val grade: Double,
	val maxGrade: Double,
	val date: Long,
	val type: EvaluationType,
	val isDone: Boolean
)
