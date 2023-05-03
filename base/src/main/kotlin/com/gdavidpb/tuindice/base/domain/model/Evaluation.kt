package com.gdavidpb.tuindice.base.domain.model

import java.util.*

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
	val isDone: Boolean
)