package com.gdavidpb.tuindice.base.domain.model.subject

data class Subject(
	val id: String,
	val quarterId: String,
	val code: String,
	val name: String,
	val credits: Int,
	val grade: Int,
	val gradingMode: GradingMode = GradingMode.NUMERIC,
	val status: SubjectStatus? = null,
	val simulationStatus: SubjectStatus? = null
)
