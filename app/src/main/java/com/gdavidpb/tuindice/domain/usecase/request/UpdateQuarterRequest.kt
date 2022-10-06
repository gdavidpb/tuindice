package com.gdavidpb.tuindice.domain.usecase.request

import com.gdavidpb.tuindice.base.data.model.database.SubjectUpdate

data class UpdateQuarterRequest(
	val qid: String,
	val sid: String,
	val update: SubjectUpdate,
	val dispatchChanges: Boolean
)
