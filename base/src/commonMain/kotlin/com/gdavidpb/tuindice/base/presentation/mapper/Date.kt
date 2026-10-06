@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.base.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import kotlinx.datetime.*
import tuindice.base.generated.resources.Res
import tuindice.base.generated.resources.date_day_month_year
import tuindice.base.generated.resources.date_day_short_month
import tuindice.base.generated.resources.date_short_weekday_numeric_date
import tuindice.base.generated.resources.date_time
import tuindice.base.generated.resources.date_time_am
import tuindice.base.generated.resources.date_time_pm
import tuindice.base.generated.resources.date_today_time
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
	WEEKDAY_NUMERIC_DATE,
	SHORT_WEEKDAY_NUMERIC_DATE,
	DAY_SHORT_MONTH
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

	return when (style) {
		DateTextStyle.TODAY_TIME ->
			UiText.Resource(Res.string.date_today_time, listOf(time))

		DateTextStyle.YESTERDAY_TIME ->
			UiText.Resource(Res.string.date_yesterday_time, listOf(time))

		DateTextStyle.WEEKDAY_TIME ->
			UiText.Resource(Res.string.date_weekday_time, listOf(dayName, time))

		DateTextStyle.DAY_MONTH_YEAR ->
			UiText.Resource(Res.string.date_day_month_year, listOf(dayOfMonth, monthName, year))

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
	}
}

// The time on a twelve-hour clock: "03:05 p. m.".
private fun LocalDateTime.toTimeText(): UiText {
	val hour12 = ((hour + 11) % 12 + 1).toString().padStart(2, '0')
	val minutes = minute.toString().padStart(2, '0')
	val period = if (hour < 12) Res.string.date_time_am else Res.string.date_time_pm

	return UiText.Resource(Res.string.date_time, listOf(hour12, minutes, UiText.Resource(period)))
}

fun Long.daysToNow() =
	Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
		.daysUntil(
			other = Instant
				.fromEpochMilliseconds(this)
				.toLocalDateTime(TimeZone.currentSystemDefault())
				.date
		)
