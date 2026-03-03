@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.base.presentation.mapper

import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class ParsedDate(
	val time: Long
)

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
	val amPm = if (dateTime.hour < 12) "AM" else "PM"

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

fun String.parseMonthYear(): ParsedDate? {

	val parts = trim().split(" ").filter { it.isNotBlank() }
	if (parts.size != 2) return null

	val monthText = normalizeLocalizedDateToken(parts[0])
	val year = parts[1].toIntOrNull() ?: return null
	val month = localizedFullMonthNames().indexOf(monthText) + 1
	if (month == 0) return null

	val localDate = runCatching { LocalDate(year, month, 1) }.getOrNull() ?: return null
	val epoch = localDate
		.atStartOfDayIn(TimeZone.currentSystemDefault())
		.toEpochMilliseconds()

	return ParsedDate(epoch)
}

fun String.parseDate(format: String): ParsedDate? {
	return when (format) {
		"MMMM yyyy" -> parseMonthYear()
		else -> null
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
