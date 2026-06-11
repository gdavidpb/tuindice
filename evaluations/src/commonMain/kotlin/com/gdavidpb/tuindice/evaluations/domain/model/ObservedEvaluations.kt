package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.base.domain.model.Evaluation

data class ObservedEvaluations(
	val evaluations: List<Evaluation>,
	val hasSyncedEvaluations: Boolean
)
