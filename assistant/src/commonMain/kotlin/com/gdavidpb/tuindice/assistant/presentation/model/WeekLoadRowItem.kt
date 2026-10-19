package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One week of the load: its number, its evaluations and its points, and whether it is heavy.
data class WeekLoadRowItem(
	val weekText: UiText,
	val detailText: UiText,
	val heavy: Boolean
)
