package com.gdavidpb.tuindice.base.domain.model

import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import java.util.Date

data class Evaluation(
	val evaluationId: String,
	val subjectId: String,
	val quarterId: String,
	val grade: Double?,
	val maxGrade: Double,
	val date: Date,
	val type: EvaluationType,
	val state: EvaluationState,
	val subject: Subject
)