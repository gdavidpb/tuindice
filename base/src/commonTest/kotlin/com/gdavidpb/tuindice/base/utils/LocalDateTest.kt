package com.gdavidpb.tuindice.base.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalDateTest {
	// The offset of the zone is what tells a zone's day from UTC's.
	private val aheadOfUtc = TimeZone.of("Asia/Tokyo")
	private val behindUtc = TimeZone.of("America/Caracas")

	@Test
	fun toLocalDate_readsTheDayInTheGivenZone() {
		// 2026-10-12 00:00 in Tokyo (UTC+9) is still 2026-10-11 in UTC.
		val startOfDay = LocalDateTime(2026, 10, 12, 0, 0).toInstant(aheadOfUtc).toEpochMilliseconds()

		assertEquals(LocalDate(2026, 10, 12), startOfDay.toLocalDate(aheadOfUtc))
		assertEquals(LocalDate(2026, 10, 11), startOfDay.toLocalDate(TimeZone.UTC))
	}

	@Test
	fun toLocalDate_ofTheLastMinuteOfTheDay_staysInThatDay() {
		val lastMinute = LocalDateTime(2026, 10, 12, 23, 59).toInstant(behindUtc).toEpochMilliseconds()

		assertEquals(LocalDate(2026, 10, 12), lastMinute.toLocalDate(behindUtc))
		// 23:59 in Caracas (UTC-4) is already the next day in UTC.
		assertEquals(LocalDate(2026, 10, 13), lastMinute.toLocalDate(TimeZone.UTC))
	}

	@Test
	fun toLocalDate_withoutAZone_readsTheDevicesOwn() {
		val zone = TimeZone.currentSystemDefault()
		val startOfDay = LocalDateTime(2026, 10, 12, 0, 0).toInstant(zone).toEpochMilliseconds()

		assertEquals(LocalDate(2026, 10, 12), startOfDay.toLocalDate())
	}
}
