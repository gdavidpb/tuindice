package com.gdavidpb.tuindice.base.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import tuindice.base.generated.resources.Res
import tuindice.base.generated.resources.date_month_names
import tuindice.base.generated.resources.date_month_short_names
import tuindice.base.generated.resources.date_month_year
import tuindice.base.generated.resources.date_weekday_names
import tuindice.base.generated.resources.date_weekday_short_names

// The names live in the resources as lists, January and Monday first. What is built here says
// which element of which list; it is read where the text is drawn.

/** The full name of a month, in lower case. */
fun Month.toNameText(): UiText = UiText.ArrayItem(Res.array.date_month_names, ordinal)

/** The three-letter name of a month, in lower case and without a period. */
fun Month.toShortNameText(): UiText = UiText.ArrayItem(Res.array.date_month_short_names, ordinal)

/** The full name of a weekday, in lower case. */
fun DayOfWeek.toNameText(): UiText = UiText.ArrayItem(Res.array.date_weekday_names, ordinal)

/** The three-letter name of a weekday, in lower case. */
fun DayOfWeek.toShortNameText(): UiText = UiText.ArrayItem(Res.array.date_weekday_short_names, ordinal)

/** The month of a date and its year: "abril 2026". */
fun LocalDate.formatLocalizedMonthYear(): UiText {
	return UiText.Resource(Res.string.date_month_year, listOf(month.toNameText(), year.toString()))
}
