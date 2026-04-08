package com.gdavidpb.tuindice.base.domain.model.subject

data class Subject(
	val id: String,
	val termId: String,
	val code: String,
	val name: String,
	val credits: Int,
	val grade: Int,
	val gradingMode: GradingMode = GradingMode.NUMERIC,
	val status: SubjectStatus? = null
)
