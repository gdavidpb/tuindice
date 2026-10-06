package com.gdavidpb.tuindice.base.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import tuindice.base.generated.resources.Res
import tuindice.base.generated.resources.date_day_month_year
import tuindice.base.generated.resources.date_day_short_month
import tuindice.base.generated.resources.date_month_names
import tuindice.base.generated.resources.date_month_short_names
import tuindice.base.generated.resources.date_short_weekday_numeric_date
import tuindice.base.generated.resources.date_time
import tuindice.base.generated.resources.date_time_am
import tuindice.base.generated.resources.date_time_pm
import tuindice.base.generated.resources.date_today_time
import tuindice.base.generated.resources.date_weekday_day_month
import tuindice.base.generated.resources.date_weekday_names
import tuindice.base.generated.resources.date_weekday_numeric_date
import tuindice.base.generated.resources.date_weekday_past_day_month
import tuindice.base.generated.resources.date_weekday_short_names
import tuindice.base.generated.resources.date_weekday_time
import tuindice.base.generated.resources.date_yesterday_time
import kotlin.test.Test
import kotlin.test.assertEquals

// What formatDate describes: the template of the style, the names by their place in the lists and
// the numbers already padded. The Spanish each style reads as is asserted in DateUiTest.
class DateTest {
	@Test
	fun formatDate_todayAndYesterday_describeOnlyTheTime() {
		assertEquals(
			UiText.Resource(Res.string.date_today_time, listOf(AFTERNOON_TIME)),
			THURSDAY_AFTERNOON.formatDate(DateTextStyle.TODAY_TIME)
		)
		assertEquals(
			UiText.Resource(Res.string.date_yesterday_time, listOf(AFTERNOON_TIME)),
			THURSDAY_AFTERNOON.formatDate(DateTextStyle.YESTERDAY_TIME)
		)
	}

	@Test
	fun formatDate_weekdayTime_describesTheWeekdayNameAndTheTime() {
		assertEquals(
			UiText.Resource(Res.string.date_weekday_time, listOf(THURSDAY, AFTERNOON_TIME)),
			THURSDAY_AFTERNOON.formatDate(DateTextStyle.WEEKDAY_TIME)
		)
	}

	@Test
	fun formatDate_time_usesATwelveHourClockWithPaddedNumbers() {
		assertEquals(
			UiText.Resource(Res.string.date_today_time, listOf(timeText("12", "30", isMorning = true))),
			millisOf(hour = 0, minute = 30).formatDate(DateTextStyle.TODAY_TIME)
		)
		assertEquals(
			UiText.Resource(Res.string.date_today_time, listOf(timeText("09", "07", isMorning = true))),
			millisOf(hour = 9, minute = 7).formatDate(DateTextStyle.TODAY_TIME)
		)
		assertEquals(
			UiText.Resource(Res.string.date_today_time, listOf(timeText("12", "00", isMorning = false))),
			millisOf(hour = 12, minute = 0).formatDate(DateTextStyle.TODAY_TIME)
		)
		assertEquals(
			UiText.Resource(Res.string.date_today_time, listOf(timeText("11", "59", isMorning = false))),
			millisOf(hour = 23, minute = 59).formatDate(DateTextStyle.TODAY_TIME)
		)
	}

	@Test
	fun formatDate_dayMonthYear_describesThePaddedDayTheMonthNameAndTheYear() {
		assertEquals(
			UiText.Resource(Res.string.date_day_month_year, listOf("05", MARCH, "2026")),
			millisOf(month = 3, day = 5).formatDate(DateTextStyle.DAY_MONTH_YEAR)
		)
	}

	@Test
	fun formatDate_weekdayAndDayMonth_describeTheWeekdayThePaddedDayAndTheMonthName() {
		assertEquals(
			UiText.Resource(Res.string.date_weekday_past_day_month, listOf(THURSDAY, "15", JANUARY)),
			THURSDAY_AFTERNOON.formatDate(DateTextStyle.WEEKDAY_PAST_DAY_MONTH)
		)
		assertEquals(
			UiText.Resource(Res.string.date_weekday_day_month, listOf(THURSDAY, "15", JANUARY)),
			THURSDAY_AFTERNOON.formatDate(DateTextStyle.WEEKDAY_DAY_MONTH)
		)
	}

	@Test
	fun formatDate_numericDates_describeTheWeekdayAndThePaddedDayMonthAndTwoDigitYear() {
		assertEquals(
			UiText.Resource(Res.string.date_weekday_numeric_date, listOf(THURSDAY, "15", "01", "26")),
			THURSDAY_AFTERNOON.formatDate(DateTextStyle.WEEKDAY_NUMERIC_DATE)
		)
		assertEquals(
			UiText.Resource(
				Res.string.date_short_weekday_numeric_date,
				listOf(UiText.ArrayItem(Res.array.date_weekday_short_names, 3), "15", "01", "26")
			),
			THURSDAY_AFTERNOON.formatDate(DateTextStyle.SHORT_WEEKDAY_NUMERIC_DATE)
		)
		assertEquals(
			UiText.Resource(
				Res.string.date_weekday_numeric_date,
				listOf(UiText.ArrayItem(Res.array.date_weekday_names, 0), "07", "12", "09")
			),
			millisOf(year = 2009, month = 12, day = 7).formatDate(DateTextStyle.WEEKDAY_NUMERIC_DATE)
		)
	}

	@Test
	fun formatDate_dayShortMonth_describesTheUnpaddedDayAndTheShortMonthName() {
		assertEquals(
			UiText.Resource(
				Res.string.date_day_short_month,
				listOf("5", UiText.ArrayItem(Res.array.date_month_short_names, 0))
			),
			millisOf(month = 1, day = 5).formatDate(DateTextStyle.DAY_SHORT_MONTH)
		)
		assertEquals(
			UiText.Resource(
				Res.string.date_day_short_month,
				listOf("22", UiText.ArrayItem(Res.array.date_month_short_names, 8))
			),
			millisOf(month = 9, day = 22).formatDate(DateTextStyle.DAY_SHORT_MONTH)
		)
	}
}

private fun timeText(hour: String, minutes: String, isMorning: Boolean): UiText {
	val period = if (isMorning) Res.string.date_time_am else Res.string.date_time_pm

	return UiText.Resource(Res.string.date_time, listOf(hour, minutes, UiText.Resource(period)))
}

// formatDate reads the instant in the zone of the device, so the instants are built in that zone.
private fun millisOf(
	year: Int = 2026,
	month: Int = 1,
	day: Int = 15,
	hour: Int = 15,
	minute: Int = 5
): Long {
	return LocalDateTime(year = year, month = month, day = day, hour = hour, minute = minute)
		.toInstant(TimeZone.currentSystemDefault())
		.toEpochMilliseconds()
}

// Thursday, January 15th 2026, 3:05 in the afternoon.
private val THURSDAY_AFTERNOON = millisOf()
private val AFTERNOON_TIME = timeText("03", "05", isMorning = false)
private val THURSDAY = UiText.ArrayItem(Res.array.date_weekday_names, 3)
private val JANUARY = UiText.ArrayItem(Res.array.date_month_names, 0)
private val MARCH = UiText.ArrayItem(Res.array.date_month_names, 2)
