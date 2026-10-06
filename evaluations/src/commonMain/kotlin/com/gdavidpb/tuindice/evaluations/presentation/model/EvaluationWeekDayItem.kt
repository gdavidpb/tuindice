package com.gdavidpb.tuindice.evaluations.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

data class EvaluationWeekDayItem(
	val weekdayText: UiText,
	val dayText: String,
	val isSelected: Boolean,
	val hasEvaluations: Boolean
)
