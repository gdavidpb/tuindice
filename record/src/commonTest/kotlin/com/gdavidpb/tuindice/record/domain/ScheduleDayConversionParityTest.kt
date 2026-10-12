@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.record.domain

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicScheduleEngine
import com.gdavidpb.tuindice.base.data.source.ScheduleClockDataSource
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

// `base` cannot see `academiccore`, so the day (1 = Sunday ... 7 = Saturday) is converted in both: the
// clock that says what day it is, and the engine that says what day a date is. This module sees both.
class ScheduleDayConversionParityTest {
	@Test
	fun theClockAndTheEngine_numberEveryDayOfTheWeekTheSame() = runTest {
		// October 11th, 2026 is a Sunday: a full week from Sunday to Saturday.
		val sunday = LocalDate(2026, 10, 11)

		(0..6).forEach { offset ->
			val date = sunday.plus(DatePeriod(days = offset))
			val clock = object : Clock {
				override fun now(): Instant = date.atStartOfDayIn(TimeZone.UTC) + 12.hours
			}
			val now = ScheduleClockDataSource(clock = clock, timeZone = { TimeZone.UTC }).observeNow().first()

			assertEquals(offset + 1, now.dayOfWeek, "clock, $date")
			assertEquals(now.dayOfWeek, AcademicScheduleEngine.dayOfWeekOf(date), "engine, $date")
			assertEquals(now.date, date)
			// And the schedule has a column for it.
			assertEquals(now.dayOfWeek, ScheduleDay.fromCode(now.dayOfWeek)?.code, "column, $date")
		}
	}
}
