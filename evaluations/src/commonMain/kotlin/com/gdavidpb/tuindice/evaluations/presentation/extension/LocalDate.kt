package com.gdavidpb.tuindice.evaluations.presentation.extension

import com.gdavidpb.tuindice.base.presentation.mapper.formatLocalizedMonthYear
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
