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
import kotlin.test.Test
import kotlin.test.assertEquals

// What the names describe: which element of which list. The Spanish they read as is asserted
// where they are resolved, in LocalizedDateFormatterUiTest.
class LocalizedDateFormatterTest {
	@Test
	fun monthNames_pointAtTheMonthByItsPlaceInTheYear() {
		assertEquals(
			List(12) { index -> UiText.ArrayItem(Res.array.date_month_names, index) },
			Month.entries.map { month -> month.toNameText() }
		)
		assertEquals(
			List(12) { index -> UiText.ArrayItem(Res.array.date_month_short_names, index) },
			Month.entries.map { month -> month.toShortNameText() }
		)
	}

	@Test
	fun weekdayNames_pointAtTheDayByItsPlaceInAWeekThatStartsOnMonday() {
		assertEquals(UiText.ArrayItem(Res.array.date_weekday_names, 0), DayOfWeek.MONDAY.toNameText())
		assertEquals(UiText.ArrayItem(Res.array.date_weekday_names, 6), DayOfWeek.SUNDAY.toNameText())
		assertEquals(
			List(7) { index -> UiText.ArrayItem(Res.array.date_weekday_short_names, index) },
			DayOfWeek.entries.map { day -> day.toShortNameText() }
		)
	}

	@Test
	fun formatLocalizedMonthYear_describesTheMonthNameAndTheYear() {
		assertEquals(
			UiText.Resource(
				Res.string.date_month_year,
				listOf(UiText.ArrayItem(Res.array.date_month_names, 3), "2026")
			),
			LocalDate(year = 2026, month = Month.APRIL, day = 1).formatLocalizedMonthYear()
		)
	}
}
