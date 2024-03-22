package com.gdavidpb.tuindice.base.domain.model

data class Evaluation(
	val evaluationId: String,
	val subjectId: String,
	val subjectCode: String,
	val quarterId: String,
	val grade: Double?,
	val maxGrade: Double,
	val date: Long?,
	val type: EvaluationType,
	val state: EvaluationState
)