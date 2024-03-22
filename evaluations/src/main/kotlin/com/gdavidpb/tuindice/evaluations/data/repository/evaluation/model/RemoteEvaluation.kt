package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.model

import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.EvaluationType

data class RemoteEvaluation(
	val evaluationId: String,
	val subjectId: String,
	val quarterId: String,
	val subjectCode: String,
	val grade: Double?,
	val maxGrade: Double,
	val date: Long?,
	val type: EvaluationType,
	val state: EvaluationState
)