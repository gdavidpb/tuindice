@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.model.UiText
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.text_last_sync
import tuindice.summary.generated.resources.text_last_sync_never
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

// What the sync line describes: which style it picks by how many days ago the sync was. The
// Spanish it reads as is asserted in DateUiTest.
class DateTest {
	@Test
	fun formatSyncTimestamp_whenThereHasBeenNoSync_describesNever() {
		assertEquals(UiText.Resource(Res.string.text_last_sync_never), (null as Long?).formatSyncTimestamp(Clock.System))
		assertEquals(UiText.Resource(Res.string.text_last_sync_never), 0L.formatSyncTimestamp(Clock.System))
	}

	@Test
	fun formatSyncTimestamp_whenSyncWasTodayOrYesterday_describesTheRelativeDayAndTheTime() {
		val today = millisDaysAgo(0)
		val yesterday = millisDaysAgo(1)

		assertEquals(
			UiText.Capitalized(today.formatDate(DateTextStyle.TODAY_TIME)),
			today.formatSyncTimestamp(Clock.System)
		)
		assertEquals(
			UiText.Capitalized(yesterday.formatDate(DateTextStyle.YESTERDAY_TIME)),
			yesterday.formatSyncTimestamp(Clock.System)
		)
	}

	@Test
	fun formatSyncTimestamp_whenSyncWasWithinTheLastWeek_describesTheCapitalizedWeekdayAndTheTime() {
		listOf(2, 6).forEach { daysAgo ->
			val instant = millisDaysAgo(daysAgo)

			assertEquals(
				UiText.Capitalized(instant.formatDate(DateTextStyle.WEEKDAY_TIME)),
				instant.formatSyncTimestamp(Clock.System),
				"$daysAgo days ago"
			)
		}
	}

	// A weekday alone names a single day only inside the last week: a sync from the same weekday a
	// week or a month ago must not read as if it were this week's.
	@Test
	fun formatSyncTimestamp_whenSyncWasAWeekAgoOrMore_describesTheDayMonthAndYear() {
		listOf(7, 30).forEach { daysAgo ->
			val instant = millisDaysAgo(daysAgo)

			assertEquals(
				UiText.Capitalized(instant.formatDate(DateTextStyle.DAY_MONTH_YEAR)),
				instant.formatSyncTimestamp(Clock.System),
				"$daysAgo days ago"
			)
		}
	}

	// Only a clock set wrong puts the last sync ahead of today; it says its date too.
	@Test
	fun formatSyncTimestamp_whenInstantIsAhead_describesTheDayMonthAndYear() {
		val tomorrow = millisDaysAgo(-1)

		assertEquals(
			UiText.Capitalized(tomorrow.formatDate(DateTextStyle.DAY_MONTH_YEAR)),
			tomorrow.formatSyncTimestamp(Clock.System)
		)
	}

	@Test
	fun toSyncStatusText_describesTheLastSyncLineWithTheTimestampInside() {
		val today = millisDaysAgo(0)

		assertEquals(
			UiText.Resource(Res.string.text_last_sync, listOf(today.formatSyncTimestamp(Clock.System))),
			today.toSyncStatusText(Clock.System)
		)
		assertEquals(
			UiText.Resource(
				Res.string.text_last_sync,
				listOf(UiText.Resource(Res.string.text_last_sync_never))
			),
			(null as Long?).toSyncStatusText(Clock.System)
		)
	}

	// "Today" and "yesterday" are told by the clock the caller hands over.
	@Test
	fun formatSyncTimestamp_countsTheDaysFromTheClockItIsGiven() {
		val sync = Instant.parse("2026-10-14T15:00:00Z").toEpochMilliseconds()

		assertEquals(
			UiText.Capitalized(sync.formatDate(DateTextStyle.YESTERDAY_TIME)),
			sync.formatSyncTimestamp(fixedClock("2026-10-15T15:00:00Z"))
		)
		assertEquals(
			UiText.Capitalized(sync.formatDate(DateTextStyle.TODAY_TIME)),
			sync.formatSyncTimestamp(fixedClock("2026-10-14T20:00:00Z"))
		)
	}

	private fun fixedClock(iso: String) = object : Clock {
		private val instant = Instant.parse(iso)

		override fun now(): Instant = instant
	}
}

// That time of the day that many days ago (ahead, when negative), in the zone the mapper reads
// the instant in.
internal fun millisDaysAgo(days: Int, hour: Int = 12, minute: Int = 0): Long {
	val timeZone = TimeZone.currentSystemDefault()
	val date: LocalDate = Clock.System.now().toLocalDateTime(timeZone).date.minus(DatePeriod(days = days))

	return LocalDateTime(
		year = date.year,
		month = date.month,
		day = date.day,
		hour = hour,
		minute = minute
	).toInstant(timeZone).toEpochMilliseconds()
}
