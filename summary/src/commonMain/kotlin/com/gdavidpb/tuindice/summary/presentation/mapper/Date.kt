package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.daysToNow
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.text_last_sync
import tuindice.summary.generated.resources.text_last_sync_never

// The line under the profile: "Última sincronización: Hoy, 03:05 p. m.".
fun Long?.toSyncStatusText(): UiText {
	return UiText.Resource(Res.string.text_last_sync, listOf(formatSyncTimestamp()))
}

// When the last sync was, told relative to the day it is asked: never, or today, yesterday or a
// weekday with its time.
fun Long?.formatSyncTimestamp(): UiText {
	if (this == null || this == 0L) return UiText.Resource(Res.string.text_last_sync_never)

	val daysDistance = daysToNow()
	val style = when {
		daysDistance == 0 -> DateTextStyle.TODAY_TIME
		daysDistance == -1 -> DateTextStyle.YESTERDAY_TIME
		daysDistance < 7 -> DateTextStyle.WEEKDAY_TIME
		else -> DateTextStyle.DAY_MONTH_YEAR
	}

	return UiText.Capitalized(formatDate(style))
}
