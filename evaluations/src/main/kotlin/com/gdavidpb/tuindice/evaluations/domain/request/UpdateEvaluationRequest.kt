package com.gdavidpb.tuindice.evaluations.domain.request

import com.gdavidpb.tuindice.base.data.model.database.EvaluationUpdate

data class UpdateEvaluationRequest(
	val eid: String,
	val update: EvaluationUpdate,
	val dispatchChanges: Boolean
)