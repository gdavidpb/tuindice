package com.gdavidpb.tuindice.base.domain.model

import java.util.*

data class Evaluation(
	val id: String,
	val sid: String,
	val subjectCode: String,
	val notes: String,
	val grade: Double,
	val maxGrade: Double,
	val date: Date,
	val type: EvaluationType,
	val isDone: Boolean
)