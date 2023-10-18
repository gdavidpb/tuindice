package com.gdavidpb.tuindice.base.domain.model

import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import java.util.Date

data class Evaluation(
	val evaluationId: String,
	val subjectId: String,
	val quarterId: String,
	val ordinal: Int,
	val grade: Double,
	val maxGrade: Double,
	val date: Date,
	val lastModified: Date,
	val type: EvaluationType,
	val subject: Subject,
	val isOverdue: Boolean,
	val isCompleted: Boolean,
	val isContinuous: Boolean = (date.time == 0L),
	val isGradeRequired: Boolean = (isOverdue && !isCompleted)
)