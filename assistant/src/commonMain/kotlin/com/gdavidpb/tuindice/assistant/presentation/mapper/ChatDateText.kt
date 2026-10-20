package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.model.UiText
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

// A moment the chat tells the student about, read in their time zone against the moment the card is read:
// "hoy a las 3:05 p. m." on that same day, "el viernes 23 de octubre, 6:53 a. m." on another day of this
// year, and the year after the day on another year.
internal fun Long.toChatDateText(nowMillis: Long): UiText {
	val date = toLocalDate()
	val today = nowMillis.toLocalDate()
	val style = when {
		date == today -> DateTextStyle.TODAY_AT_CLOCK
		date.year == today.year -> DateTextStyle.WEEKDAY_DAY_MONTH_AT_CLOCK
		else -> DateTextStyle.WEEKDAY_DAY_MONTH_YEAR_AT_CLOCK
	}

	return formatDate(style)
}

private fun Long.toLocalDate(): LocalDate =
	Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault()).date
