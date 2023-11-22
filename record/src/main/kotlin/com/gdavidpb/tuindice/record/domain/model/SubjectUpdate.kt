package com.gdavidpb.tuindice.record.domain.model

data class SubjectUpdate(
	val quarterId: String,
	val subjectId: String,
	val grade: Int,
	val dispatchToRemote: Boolean
)