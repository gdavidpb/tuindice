package com.gdavidpb.tuindice.record.domain.usecase.param

data class UpdateSubjectParams(
	val subjectId: String,
	val grade: Int,
	val dispatchToRemote: Boolean
)