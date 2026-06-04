package com.gdavidpb.tuindice.evaluations.presentation.model

data class EvaluationsWeekGroupItem(
	val weekNumber: Int,
	val title: String,
	val groups: List<EvaluationsGroupItem>
)
