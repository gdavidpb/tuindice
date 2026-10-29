package com.gdavidpb.tuindice.base.ui.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One week of a week strip: the key that names it, the tag it is known by on screen, its label, its seven days, and
// whether it is the week that holds today.
data class WeekStripItem<K>(
	val key: K,
	val tagSuffix: String,
	val labelText: UiText,
	val days: List<WeekStripDayItem>,
	val isCurrent: Boolean
)
