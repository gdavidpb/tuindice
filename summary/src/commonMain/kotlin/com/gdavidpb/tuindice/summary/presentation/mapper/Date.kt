package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.daysToNow
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.text_last_sync
import tuindice.summary.generated.resources.text_last_sync_never

private const val DAYS_IN_A_WEEK = 7

// The line under the profile: "Última sincronización: Hoy, 03:05 p. m.".
fun Long?.toSyncStatusText(): UiText {
	return UiText.Resource(Res.string.text_last_sync, listOf(formatSyncTimestamp()))
}

// When the last sync was, told relative to the day it is asked: never, today, yesterday, a weekday
// while it is within the last week, and the date from then on.
fun Long?.formatSyncTimestamp(): UiText {
	if (this == null || this == 0L) return UiText.Resource(Res.string.text_last_sync_never)

	// daysToNow counts from today to the sync, so a day in the past is negative. A weekday names
	// a single day only inside the last week; anything older (or ahead, on a clock set wrong) says
	// its date.
	val daysAgo = -daysToNow()
	val style = when {
		daysAgo == 0 -> DateTextStyle.TODAY_TIME
		daysAgo == 1 -> DateTextStyle.YESTERDAY_TIME
		daysAgo in 2 until DAYS_IN_A_WEEK -> DateTextStyle.WEEKDAY_TIME
		else -> DateTextStyle.DAY_MONTH_YEAR
	}

	return UiText.Capitalized(formatDate(style))
}
