package com.gdavidpb.tuindice.base.domain.model

import java.util.Date

data class Evaluation(
	val evaluationId: String,
	val subjectId: String,
	val quarterId: String,
	val subjectCode: String,
	val subjectName: String,
	val name: String,
	val grade: Double,
	val maxGrade: Double,
	val date: Date,
	val lastModified: Date,
	val type: EvaluationType,
	val isOverdue: Boolean,
	val isCompleted: Boolean,
	val isContinuous: Boolean = (date.time == 0L),
	val isAttentionRequired: Boolean = (isOverdue && !isContinuous && !isCompleted)
)