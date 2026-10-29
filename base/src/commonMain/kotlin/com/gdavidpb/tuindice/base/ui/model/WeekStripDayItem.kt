package com.gdavidpb.tuindice.base.ui.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One day of a week strip: its weekday, its number, whether it is the selected day, and whether it holds an evaluation.
data class WeekStripDayItem(
	val weekdayText: UiText,
	val dayText: String,
	val isSelected: Boolean,
	val hasEvaluations: Boolean
)
