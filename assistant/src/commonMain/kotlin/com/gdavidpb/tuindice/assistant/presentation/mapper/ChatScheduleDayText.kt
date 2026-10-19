package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.mapper.toNameText
import com.gdavidpb.tuindice.base.presentation.model.UiText
import kotlinx.datetime.DayOfWeek

// The name of a day of a schedule, capitalized as the app writes the days of the week: «Lunes» for 1, the Monday.
// A day outside the week, such as 0 or 8, has no name.
fun scheduleDayText(dayOfWeek: Int): UiText? =
	DayOfWeek.entries.getOrNull(dayOfWeek - 1)?.let { day -> UiText.Capitalized(day.toNameText()) }
