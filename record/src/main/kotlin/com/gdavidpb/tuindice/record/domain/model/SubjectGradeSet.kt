package com.gdavidpb.tuindice.record.domain.model

data class SubjectGradeSet(
	val id: String,
	val quarterId: String,
	val grade: Int,
	val dispatchToRemote: Boolean
)