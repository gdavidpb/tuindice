package com.gdavidpb.tuindice.evaluations.presentation.utils

import com.gdavidpb.tuindice.base.presentation.mapper.formatLocalizedMonthYear
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.Instant

fun LocalDate.toCalendarGrid(): List<LocalDate?> {
	val leadingEmptyDays = dayOfWeek.ordinal
	val daysInMonth = daysUntil(nextMonthStart())
	val cells = MutableList<LocalDate?>(leadingEmptyDays) { null }

	for (day in 1..daysInMonth) {
		cells += LocalDate(year = year, month = month, day = day)
	}

	while (cells.size % 7 != 0) {
		cells += null
	}

	return cells
}

fun LocalDate.monthStart() = LocalDate(year = year, month = month, day = 1)

fun LocalDate.previousMonthStart() = if (month.ordinal == 0) {
	LocalDate(year = year - 1, month = Month.DECEMBER, day = 1)
} else {
	LocalDate(year = year, month = Month.entries[month.ordinal - 1], day = 1)
}

fun LocalDate.nextMonthStart() = if (month.ordinal == 11) {
	LocalDate(year = year + 1, month = Month.JANUARY, day = 1)
} else {
	LocalDate(year = year, month = Month.entries[month.ordinal + 1], day = 1)
}

fun LocalDate.formatMonthYear(): String {
	return formatLocalizedMonthYear()
}

fun Long.toEvaluationLocalDate(): LocalDate {
	return Instant
		.fromEpochMilliseconds(this)
		.toLocalDateTime(TimeZone.currentSystemDefault())
		.date
}

fun LocalDate.toEvaluationEpochMillis(): Long {
	return atStartOfDayIn(TimeZone.currentSystemDefault())
		.toEpochMilliseconds()
}

fun currentEvaluationLocalDate(): LocalDate {
	return Clock.System.now()
		.toLocalDateTime(TimeZone.currentSystemDefault())
		.date
}

// Evaluation dates are constrained to the current term plus one month of slack:
// from the first day of the term's start month to the last day of the month right
// after it ends (e.g. a January–March term allows January 1st through April 30th).
fun EvaluationTermDescriptor.toSelectableDateRange(): ClosedRange<LocalDate> {
	val start = firstDayOfMonth(year = periodYear, monthNumber = periodCode.startMonth)
	val slackMonthStart = firstDayOfMonth(year = periodYear, monthNumber = periodCode.endMonth + 1)
	val endInclusive = slackMonthStart.nextMonthStart().minus(DatePeriod(days = 1))

	return start..endInclusive
}

// Snaps a date to a month within [range]: its own month when inside, otherwise the
// range's first or last allowed month.
fun LocalDate.clampToMonthRange(range: ClosedRange<LocalDate>?): LocalDate {
	if (range == null) return monthStart()

	return when {
		this < range.start -> range.start.monthStart()
		this > range.endInclusive -> range.endInclusive.monthStart()
		else -> monthStart()
	}
}

private const val MonthsPerYear = 12

private fun firstDayOfMonth(year: Int, monthNumber: Int): LocalDate {
	val normalizedYear = year + (monthNumber - 1) / MonthsPerYear
	val normalizedMonth = ((monthNumber - 1) % MonthsPerYear) + 1

	return LocalDate(
		year = normalizedYear,
		month = Month.entries[normalizedMonth - 1],
		day = 1
	)
}
