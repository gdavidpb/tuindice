@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.base.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import kotlinx.datetime.*
import tuindice.base.generated.resources.Res
import tuindice.base.generated.resources.date_day_month
import tuindice.base.generated.resources.date_day_month_year
import tuindice.base.generated.resources.date_day_month_year_unpadded
import tuindice.base.generated.resources.date_day_short_month
import tuindice.base.generated.resources.date_short_weekday_numeric_date
import tuindice.base.generated.resources.date_time
import tuindice.base.generated.resources.date_time_am
import tuindice.base.generated.resources.date_time_pm
import tuindice.base.generated.resources.date_today_at_clock
import tuindice.base.generated.resources.date_today_clock
import tuindice.base.generated.resources.date_today_time
import tuindice.base.generated.resources.date_weekday_day_month_at_clock
import tuindice.base.generated.resources.date_weekday_day_month_clock
import tuindice.base.generated.resources.date_weekday_day_month_year_at_clock
import tuindice.base.generated.resources.date_weekday_day_month_year_clock
import tuindice.base.generated.resources.date_weekday_numeric_date
import tuindice.base.generated.resources.date_weekday_time
import tuindice.base.generated.resources.date_yesterday_time
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

enum class DateTextStyle {
	TODAY_TIME,
	YESTERDAY_TIME,
	WEEKDAY_TIME,
	DAY_MONTH_YEAR,
	DAY_MONTH,
	DAY_MONTH_YEAR_UNPADDED,
	WEEKDAY_NUMERIC_DATE,
	SHORT_WEEKDAY_NUMERIC_DATE,
	DAY_SHORT_MONTH,
	TIME,
	TODAY_AT_CLOCK,
	WEEKDAY_DAY_MONTH_AT_CLOCK,
	WEEKDAY_DAY_MONTH_YEAR_AT_CLOCK
}

// Describes the text of a date: the template of its style, the names by their place in the lists
// of the resources and the numbers already padded. Nothing is read here; asString() reads it.
fun Long.formatDate(style: DateTextStyle): UiText {
	val systemTimeZone = TimeZone.currentSystemDefault()
	val dateTime = Instant
		.fromEpochMilliseconds(this)
		.toLocalDateTime(systemTimeZone)

	val dayName = dateTime.dayOfWeek.toNameText()
	val shortDayName = dateTime.dayOfWeek.toShortNameText()
	val monthName = dateTime.month.toNameText()
	val shortMonthName = dateTime.month.toShortNameText()
	val dayOfMonth = dateTime.day.toString().padStart(2, '0')
	val monthNumber = (dateTime.month.ordinal + 1).toString().padStart(2, '0')
	val year = dateTime.year.toString()
	val shortYear = (dateTime.year % 100).toString().padStart(2, '0')
	val time = dateTime.toTimeText()
	val clock = dateTime.toClockText()
	val dayNumber = dateTime.day.toString()

	return when (style) {
		DateTextStyle.TODAY_TIME ->
			UiText.Resource(Res.string.date_today_time, listOf(time))

		DateTextStyle.YESTERDAY_TIME ->
			UiText.Resource(Res.string.date_yesterday_time, listOf(time))

		DateTextStyle.WEEKDAY_TIME ->
			UiText.Resource(Res.string.date_weekday_time, listOf(dayName, time))

		DateTextStyle.DAY_MONTH_YEAR ->
			UiText.Resource(Res.string.date_day_month_year, listOf(dayOfMonth, monthName, year))

		// The day without its zero and the month by name, as a sentence says it: "5 de marzo".
		DateTextStyle.DAY_MONTH ->
			UiText.Resource(Res.string.date_day_month, listOf(dayNumber, monthName))

		DateTextStyle.DAY_MONTH_YEAR_UNPADDED ->
			UiText.Resource(Res.string.date_day_month_year_unpadded, listOf(dayNumber, monthName, year))

		DateTextStyle.WEEKDAY_NUMERIC_DATE ->
			UiText.Resource(
				Res.string.date_weekday_numeric_date,
				listOf(dayName, dayOfMonth, monthNumber, shortYear)
			)

		DateTextStyle.SHORT_WEEKDAY_NUMERIC_DATE ->
			UiText.Resource(
				Res.string.date_short_weekday_numeric_date,
				listOf(shortDayName, dayOfMonth, monthNumber, shortYear)
			)

		DateTextStyle.DAY_SHORT_MONTH ->
			UiText.Resource(Res.string.date_day_short_month, listOf(dateTime.day.toString(), shortMonthName))

		DateTextStyle.TIME -> time

		DateTextStyle.TODAY_AT_CLOCK ->
			UiText.Resource(Res.string.date_today_at_clock, listOf(clock))

		DateTextStyle.WEEKDAY_DAY_MONTH_AT_CLOCK ->
			UiText.Resource(
				Res.string.date_weekday_day_month_at_clock,
				listOf(dayName, dayNumber, monthName, clock)
			)

		DateTextStyle.WEEKDAY_DAY_MONTH_YEAR_AT_CLOCK ->
			UiText.Resource(
				Res.string.date_weekday_day_month_year_at_clock,
				listOf(dayName, dayNumber, monthName, year, clock)
			)
	}
}

// The text of a cell of a card, for the moment the style names: see DateCellStyle.
fun Long.formatDateCell(style: DateCellStyle): UiText {
	val dateTime = Instant
		.fromEpochMilliseconds(this)
		.toLocalDateTime(TimeZone.currentSystemDefault())

	val dayName = dateTime.dayOfWeek.toNameText()
	val monthName = dateTime.month.toNameText()
	val dayNumber = dateTime.day.toString()
	val year = dateTime.year.toString()
	val clock = dateTime.toClockText()

	return when (style) {
		DateCellStyle.TODAY_CLOCK ->
			UiText.Resource(Res.string.date_today_clock, listOf(clock))

		DateCellStyle.WEEKDAY_DAY_MONTH_CLOCK ->
			UiText.Resource(
				Res.string.date_weekday_day_month_clock,
				listOf(dayName, dayNumber, monthName, clock)
			)

		DateCellStyle.WEEKDAY_DAY_MONTH_YEAR_CLOCK ->
			UiText.Resource(
				Res.string.date_weekday_day_month_year_clock,
				listOf(dayName, dayNumber, monthName, year, clock)
			)
	}
}

// The time on a twelve-hour clock with the hour padded, as a date writes it: "03:05 p. m.".
private fun LocalDateTime.toTimeText(): UiText = twelveHourTime(hourText = hourOfTwelveHours().padStart(2, '0'))

// The same time without the zero before the hour, "3:05 p. m.": the clock the chat uses for its moments, as it
// uses it for the hours of its classes.
private fun LocalDateTime.toClockText(): UiText = twelveHourTime(hourText = hourOfTwelveHours())

private fun LocalDateTime.hourOfTwelveHours(): String = ((hour + 11) % 12 + 1).toString()

private fun LocalDateTime.twelveHourTime(hourText: String): UiText {
	val minutes = minute.toString().padStart(2, '0')
	val period = if (hour < 12) Res.string.date_time_am else Res.string.date_time_pm

	return UiText.Resource(Res.string.date_time, listOf(hourText, minutes, UiText.Resource(period)))
}

fun Long.daysToNow(clock: Clock) =
	clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
		.daysUntil(
			other = Instant
				.fromEpochMilliseconds(this)
				.toLocalDateTime(TimeZone.currentSystemDefault())
				.date
		)
