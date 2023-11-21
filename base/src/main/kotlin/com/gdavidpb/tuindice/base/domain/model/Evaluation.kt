package com.gdavidpb.tuindice.base.domain.model

import com.gdavidpb.tuindice.base.domain.model.subject.Subject

data class Evaluation(
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