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

// What the sync line describes: which style it picks by how many days ago the sync was. The
// Spanish it reads as is asserted in DateUiTest.
class DateTest {
	@Test
	fun formatSyncTimestamp_whenThereHasBeenNoSync_describesNever() {
		assertEquals(UiText.Resource(Res.string.text_last_sync_never), (null as Long?).formatSyncTimestamp())
		assertEquals(UiText.Resource(Res.string.text_last_sync_never), 0L.formatSyncTimestamp())
	}

	@Test
	fun formatSyncTimestamp_whenSyncWasTodayOrYesterday_describesTheRelativeDayAndTheTime() {
		val today = millisDaysAgo(0)
		val yesterday = millisDaysAgo(1)

		assertEquals(
			UiText.Capitalized(today.formatDate(DateTextStyle.TODAY_TIME)),
			today.formatSyncTimestamp()
		)
		assertEquals(
			UiText.Capitalized(yesterday.formatDate(DateTextStyle.YESTERDAY_TIME)),
			yesterday.formatSyncTimestamp()
		)
	}

	// Characterization of what the screen shows today, kept because moving the texts to resources
	// must not change any of them: the distance to a day in the past is negative, so "less than
	// seven" holds however long ago the sync was and the day-month-year style is only reached by
	// an instant a week or more ahead.
	@Test
	fun formatSyncTimestamp_whenSyncWasEarlier_describesTheCapitalizedWeekdayAndTheTime() {
		val twoDaysAgo = millisDaysAgo(2)
		val aMonthAgo = millisDaysAgo(30)

		assertEquals(
			UiText.Capitalized(twoDaysAgo.formatDate(DateTextStyle.WEEKDAY_TIME)),
			twoDaysAgo.formatSyncTimestamp()
		)
		assertEquals(
			UiText.Capitalized(aMonthAgo.formatDate(DateTextStyle.WEEKDAY_TIME)),
			aMonthAgo.formatSyncTimestamp()
		)
	}

	@Test
	fun formatSyncTimestamp_whenInstantIsAWeekOrMoreAhead_describesTheDayMonthAndYear() {
		val inSixDays = millisDaysAgo(-6)
		val inAWeek = millisDaysAgo(-7)

		assertEquals(
			UiText.Capitalized(inSixDays.formatDate(DateTextStyle.WEEKDAY_TIME)),
			inSixDays.formatSyncTimestamp()
		)
		assertEquals(
			UiText.Capitalized(inAWeek.formatDate(DateTextStyle.DAY_MONTH_YEAR)),
			inAWeek.formatSyncTimestamp()
		)
	}

	@Test
	fun toSyncStatusText_describesTheLastSyncLineWithTheTimestampInside() {
		val today = millisDaysAgo(0)

		assertEquals(
			UiText.Resource(Res.string.text_last_sync, listOf(today.formatSyncTimestamp())),
			today.toSyncStatusText()
		)
		assertEquals(
			UiText.Resource(
				Res.string.text_last_sync,
				listOf(UiText.Resource(Res.string.text_last_sync_never))
			),
			(null as Long?).toSyncStatusText()
		)
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
