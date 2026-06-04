package com.gdavidpb.tuindice.evaluations.presentation.model

data class EvaluationsWeekItem(
	val weekNumber: Int = 1,
	val labelText: String,
	val days: List<EvaluationWeekDayItem>,
	val isCurrent: Boolean = days.any { day -> day.isSelected }
)
