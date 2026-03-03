package com.gdavidpb.tuindice.evaluations.presentation.extension

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

internal fun LocalDate.toCalendarGrid(): List<LocalDate?> {
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

internal fun LocalDate.monthStart() = LocalDate(year = year, month = month, day = 1)

internal fun LocalDate.previousMonthStart() = if (month.ordinal == 0) {
	LocalDate(year = year - 1, month = Month.DECEMBER, day = 1)
} else {
	LocalDate(year = year, month = Month.entries[month.ordinal - 1], day = 1)
}

internal fun LocalDate.nextMonthStart() = if (month.ordinal == 11) {
	LocalDate(year = year + 1, month = Month.JANUARY, day = 1)
} else {
	LocalDate(year = year, month = Month.entries[month.ordinal + 1], day = 1)
}

internal fun LocalDate.formatMonthYear(): String {
	return "${monthNames[month.ordinal]} $year"
}

internal fun Long.toEvaluationLocalDate(): LocalDate {
	return Instant
		.fromEpochMilliseconds(this)
		.toLocalDateTime(TimeZone.currentSystemDefault())
		.date
}

internal fun LocalDate.toEvaluationEpochMillis(): Long {
	return atStartOfDayIn(TimeZone.currentSystemDefault())
		.toEpochMilliseconds()
}

internal fun currentEvaluationLocalDate(): LocalDate {
	return Clock.System.now()
		.toLocalDateTime(TimeZone.currentSystemDefault())
		.date
}

private val monthNames = listOf(
	"enero",
	"febrero",
	"marzo",
	"abril",
	"mayo",
	"junio",
	"julio",
	"agosto",
	"septiembre",
	"octubre",
	"noviembre",
	"diciembre"
)
