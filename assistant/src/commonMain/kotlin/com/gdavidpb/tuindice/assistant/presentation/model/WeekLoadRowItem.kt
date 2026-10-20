package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One week of the load: its number, its evaluations and its points, and the chip when the week is heavy.
data class WeekLoadRowItem(
	val weekText: UiText,
	val detailText: UiText,
	val chip: CardChipItem?
)
