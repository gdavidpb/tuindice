@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.base.presentation.mapper

import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

enum class DateTextStyle {
	TODAY_TIME,
	YESTERDAY_TIME,
	WEEKDAY_TIME,
	DAY_MONTH_YEAR,
	WEEKDAY_PAST_DAY_MONTH,
	WEEKDAY_DAY_MONTH,
	WEEKDAY_NUMERIC_DATE,
	SHORT_WEEKDAY_NUMERIC_DATE
}

fun Long.formatDate(style: DateTextStyle): String? {
	val systemTimeZone = TimeZone.currentSystemDefault()
	val dateTime = Instant
		.fromEpochMilliseconds(this)
		.toLocalDateTime(systemTimeZone)

	val dayName = localizedFullWeekdayNames()[dateTime.dayOfWeek.ordinal]
	val shortDayName = localizedShortWeekdayNames()[dateTime.dayOfWeek.ordinal]
	val monthName = localizedFullMonthNames()[dateTime.month.ordinal]
	val dayOfMonth = dateTime.day.toString().padStart(2, '0')
	val monthNumber = (dateTime.month.ordinal + 1).toString().padStart(2, '0')
	val year = dateTime.year.toString()
	val shortYear = (dateTime.year % 100).toString().padStart(2, '0')
	val hour12 = ((dateTime.hour + 11) % 12 + 1).toString().padStart(2, '0')
	val minutes = dateTime.minute.toString().padStart(2, '0')
	val amPm = if (dateTime.hour < 12) "a. m." else "p. m."

	return when (style) {
		DateTextStyle.TODAY_TIME -> "Hoy, $hour12:$minutes $amPm"
		DateTextStyle.YESTERDAY_TIME -> "Ayer, $hour12:$minutes $amPm"
		DateTextStyle.WEEKDAY_TIME -> "$dayName, $hour12:$minutes $amPm"
		DateTextStyle.DAY_MONTH_YEAR -> "$dayOfMonth de $monthName $year"
		DateTextStyle.WEEKDAY_PAST_DAY_MONTH -> "$dayName pasado — $dayOfMonth de $monthName"
		DateTextStyle.WEEKDAY_DAY_MONTH -> "$dayName — $dayOfMonth de $monthName"
		DateTextStyle.WEEKDAY_NUMERIC_DATE -> "$dayName — $dayOfMonth/$monthNumber/$shortYear"
		DateTextStyle.SHORT_WEEKDAY_NUMERIC_DATE -> "$shortDayName — $dayOfMonth/$monthNumber/$shortYear"
	}
}

fun Long.daysToNow() =
	Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
		.daysUntil(
			other = Instant
				.fromEpochMilliseconds(this)
				.toLocalDateTime(TimeZone.currentSystemDefault())
				.date
		)

fun Long.weeksToNow() =
	Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
		.until(
			other = Instant
				.fromEpochMilliseconds(this)
				.toLocalDateTime(TimeZone.currentSystemDefault())
				.date,
			unit = DateTimeUnit.WEEK
		)
