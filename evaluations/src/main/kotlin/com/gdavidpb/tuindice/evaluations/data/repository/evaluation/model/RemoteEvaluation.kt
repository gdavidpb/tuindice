package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.model

import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject

data class RemoteEvaluation(
	val evaluationId: String,
	val subjectId: String,
	val quarterId: String,
	val grade: Double?,
	val maxGrade: Double,
	val date: Long,
	val type: EvaluationType,
	val state: EvaluationState,
	val subject: Subject
)