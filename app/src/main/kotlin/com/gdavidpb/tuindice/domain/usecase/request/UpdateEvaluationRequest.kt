package com.gdavidpb.tuindice.domain.usecase.request

import com.gdavidpb.tuindice.base.data.model.database.EvaluationUpdate

data class UpdateEvaluationRequest(
	val eid: String,
	val update: EvaluationUpdate,
	val dispatchChanges: Boolean
)