package com.gdavidpb.tuindice.base.utils.extension

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.Test
import kotlin.test.assertEquals

class AcademicTermTest {
	@Test
	fun displayName_formatsSpanishMonthRange_whenTermStaysWithinSameYear() {
		assertEquals(
			"Abril - Julio 2026",
			academicTermDisplayName(
				startAtMillis = millisAtUtc(year = 2026, month = Month.APRIL, day = 1),
				endAtMillis = millisAtUtc(year = 2026, month = Month.JULY, day = 31),
			)
		)
	}

	@Test
	fun shortDisplayName_usesAbbreviatedMonthNames() {
		assertEquals(
			"Abr. - Jul. 2026",
			academicTermShortDisplayName(
				startAtMillis = millisAtUtc(year = 2026, month = Month.APRIL, day = 1),
				endAtMillis = millisAtUtc(year = 2026, month = Month.JULY, day = 31),
			)
		)
	}

	@Test
	fun displayName_includesBothYears_whenTermSpansNewYear() {
		assertEquals(
			"Diciembre 2026 - Enero 2027",
			academicTermDisplayName(
				startAtMillis = millisAtUtc(year = 2026, month = Month.DECEMBER, day = 1),
				endAtMillis = millisAtUtc(year = 2027, month = Month.JANUARY, day = 31),
			)
		)
	}
}

private fun millisAtUtc(year: Int, month: Month, day: Int): Long {
	return LocalDate(year = year, month = month, day = day)
		.atStartOfDayIn(TimeZone.UTC)
		.toEpochMilliseconds()
}
