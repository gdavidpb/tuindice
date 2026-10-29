package com.gdavidpb.tuindice.assistant.presentation.testing

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAcademicCalendar
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant

// A calendar for the tests: week 1 opens on [weekOneStart], and the weeks run seven days each, from 1 to 12. The engine
// that counts the real weeks lives in maincore; the assistant only reads the weeks it is given.
internal class TermCalendarFixture(
	override val weekOneStart: LocalDate
) : AssistantAcademicCalendar {
	override val weeks: IntRange = 1..12

	override fun weekOf(date: LocalDate): Int {
		val weekNumber = (date.toEpochDays() - weekOneStart.toEpochDays()).floorDiv(DAYS_PER_WEEK) + 1

		return weekNumber.toInt().coerceIn(weeks)
	}
}

// The instant of noon on [date] in the system time zone, which is the instant a row of the tests carries for that day.
internal fun millisOf(date: LocalDate): Long {
	return date.atTime(hour = 12, minute = 0).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}

private const val DAYS_PER_WEEK = 7L
