package com.gdavidpb.tuindice.record.domain.usecase.param

import com.gdavidpb.tuindice.record.domain.model.SubjectUpdate

data class UpdateQuarterParams(
	val quarterId: String,
	val subjectsUpdates: List<SubjectUpdate>,
	val dispatchToRemote: Boolean
)