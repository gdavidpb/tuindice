package com.gdavidpb.tuindice.evaluations.domain.param

import com.gdavidpb.tuindice.base.data.model.database.EvaluationUpdate

data class UpdateEvaluationParams(
	val eid: String,
	val update: EvaluationUpdate,
	val dispatchChanges: Boolean
)