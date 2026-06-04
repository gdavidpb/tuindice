package com.gdavidpb.tuindice.evaluations.presentation.model

data class EvaluationsWeekGroupItem(
	val key: EvaluationsWeekKey,
	val title: String,
	val groups: List<EvaluationsGroupItem>
)
