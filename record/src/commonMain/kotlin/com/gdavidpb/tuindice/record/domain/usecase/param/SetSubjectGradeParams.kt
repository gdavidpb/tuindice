package com.gdavidpb.tuindice.record.domain.usecase.param

data class SetSubjectGradeParams(
	val quarterId: String,
	val subjectId: String,
	val grade: Int,
	val commit: Boolean
)