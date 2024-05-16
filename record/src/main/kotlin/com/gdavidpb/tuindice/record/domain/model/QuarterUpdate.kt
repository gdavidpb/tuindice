package com.gdavidpb.tuindice.record.domain.model

data class QuarterUpdate(
	val id: String,
	val subjectsUpdates: List<SubjectUpdate>,
	val dispatchToRemote: Boolean
)