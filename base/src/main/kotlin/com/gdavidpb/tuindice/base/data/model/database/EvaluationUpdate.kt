package com.gdavidpb.tuindice.base.data.model.database

import com.google.firebase.Timestamp

data class EvaluationUpdate(
	val notes: String,
	val grade: Double,
	val maxGrade: Double,
	val date: Timestamp,
	val type: Int,
	val isDone: Boolean
)