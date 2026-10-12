package com.gdavidpb.tuindice.evaluations.presentation.utils

import com.gdavidpb.tuindice.base.presentation.mapper.formatLocalizedMonthYear
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.mapper.academicWeekOneStart
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

fun LocalDate.formatMonthYear(): UiText {
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

fun Clock.currentEvaluationLocalDate(): LocalDate {
	return now()
		.toLocalDateTime(TimeZone.currentSystemDefault())
		.date
}

// Evaluation dates are constrained to the current term plus one month of slack:
// from the first day of the term's start month to the last day of the month right
// after it ends (e.g. a January–March term allows January 1st through April 30th).
// The range also reaches back to the Monday that opens week 1 when that falls before the
// start month (classes starting on March 30th for an April term), so every day of week 1
// can be picked.
fun EvaluationTermDescriptor.toSelectableDateRange(): ClosedRange<LocalDate> {
	val start = minOf(
		firstDayOfMonth(year = periodYear, monthNumber = periodCode.startMonth),
		academicWeekOneStart()
	)
	val slackMonthStart = firstDayOfMonth(year = periodYear, monthNumber = periodCode.endMonth + 1)
	val endInclusive = slackMonthStart.nextMonthStart().minus(DatePeriod(days = 1))

	return start..endInclusive
}

// The month a picker over [range] opens on for a date: its own month when inside, otherwise the
// range's first or last allowed month. It never opens before the month the period starts in: the
// range may begin on the Monday of week 1, a few days into the previous month, and that month has
// only those days enabled. Going back to it stays possible (the range still holds them).
fun LocalDate.clampToMonthRange(range: ClosedRange<LocalDate>?): LocalDate {
	if (range == null) return monthStart()

	val firstMonth = range.openingMonthStart()
	val lastMonth = range.endInclusive.monthStart()
	val month = when {
		this < range.start -> firstMonth
		this > range.endInclusive -> lastMonth
		else -> monthStart()
	}

	return maxOf(month, firstMonth)
}

// The month the picker opens on. A date the student already chose, when it is inside the range,
// wins whatever month it is in: the day must be in view even if it is one of the few days of week 1
// that fall before the month the term starts in. Without one (or with one outside the range, which
// is snapped like any other date), it opens on today's month under the rule of clampToMonthRange.
fun initialPickerMonth(
	chosenDate: LocalDate?,
	today: LocalDate,
	range: ClosedRange<LocalDate>?
): LocalDate {
	if (chosenDate != null && (range == null || chosenDate in range)) return chosenDate.monthStart()

	return (chosenDate ?: today).clampToMonthRange(range)
}

// The first month whose first day is selectable: the range's own month, or the next one when the
// range begins mid-month (the Monday of week 1 falling before the start month).
private fun ClosedRange<LocalDate>.openingMonthStart(): LocalDate {
	val firstMonth = if (start.day == 1) start else start.nextMonthStart()

	return minOf(firstMonth, endInclusive.monthStart())
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
