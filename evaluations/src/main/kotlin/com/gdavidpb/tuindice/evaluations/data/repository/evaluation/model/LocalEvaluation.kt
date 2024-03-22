package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.model

import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.EvaluationType

data class LocalEvaluation(
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