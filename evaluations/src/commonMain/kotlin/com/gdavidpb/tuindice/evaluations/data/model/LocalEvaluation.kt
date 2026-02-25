package com.gdavidpb.tuindice.evaluations.data.model

data class LocalEvaluation(
	val id: String,
	val subjectId: String,
	val subjectCode: String,
	val quarterId: String,
	val grade: Double?,
	val maxGrade: Double,
	val date: Long?,
	val type: Int,
	val isDone: Boolean
)