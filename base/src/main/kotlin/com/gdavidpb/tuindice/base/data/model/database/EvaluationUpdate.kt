package com.gdavidpb.tuindice.base.data.model.database

import java.util.Date

data class EvaluationUpdate(
	val notes: String,
	val grade: Double,
	val maxGrade: Double,
	val date: Date,
	val type: Int,
	val isDone: Boolean
)