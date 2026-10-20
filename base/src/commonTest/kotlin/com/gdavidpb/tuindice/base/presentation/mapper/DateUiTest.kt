package com.gdavidpb.tuindice.base.presentation.mapper

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test

// The Spanish each style reads as, resolved the way a screen does it: asString() inside a
// composable. It reads resources, so it runs on the iOS host only (androidHostTestExcludedPatterns).
@OptIn(ExperimentalTestApi::class)
class DateUiTest {
	@Test
	fun when_styleIsTodayTime_then_readsTodayAndTheTimeOnATwelveHourClock() = runTuIndiceUiTest {
		assertDateText("Hoy, 03:05 p. m.", millisOf(hour = 15, minute = 5), DateTextStyle.TODAY_TIME)
	}

	@Test
	fun when_styleIsTodayTimeAtMidnightOrNoon_then_readsTwelveWithItsHalfOfTheDay() = runTuIndiceUiTest {
		assertDateTexts(
			"Hoy, 12:30 a. m." to millisOf(hour = 0, minute = 30).formatDate(DateTextStyle.TODAY_TIME),
			"Hoy, 12:00 p. m." to millisOf(hour = 12, minute = 0).formatDate(DateTextStyle.TODAY_TIME),
			"Hoy, 11:59 p. m." to millisOf(hour = 23, minute = 59).formatDate(DateTextStyle.TODAY_TIME)
		)
	}

	@Test
	fun when_styleIsYesterdayTime_then_readsYesterdayAndTheTime() = runTuIndiceUiTest {
		assertDateText("Ayer, 09:07 a. m.", millisOf(hour = 9, minute = 7), DateTextStyle.YESTERDAY_TIME)
	}

	@Test
	fun when_styleIsWeekdayTime_then_readsTheWeekdayInLowerCaseAndTheTime() = runTuIndiceUiTest {
		assertDateText("jueves, 03:05 p. m.", millisOf(hour = 15, minute = 5), DateTextStyle.WEEKDAY_TIME)
	}

	@Test
	fun when_styleIsDayMonthYear_then_readsThePaddedDayTheMonthAndTheYear() = runTuIndiceUiTest {
		assertDateTexts(
			"15 de enero 2026" to millisOf().formatDate(DateTextStyle.DAY_MONTH_YEAR),
			"05 de marzo 2026" to millisOf(month = 3, day = 5).formatDate(DateTextStyle.DAY_MONTH_YEAR),
			"31 de diciembre 2009" to millisOf(year = 2009, month = 12, day = 31)
				.formatDate(DateTextStyle.DAY_MONTH_YEAR)
		)
	}

	@Test
	fun when_styleIsWeekdayNumericDate_then_readsTheWeekdayAndThePaddedNumbers() = runTuIndiceUiTest {
		assertDateTexts(
			"jueves — 15/01/26" to millisOf().formatDate(DateTextStyle.WEEKDAY_NUMERIC_DATE),
			"lunes — 07/12/09" to millisOf(year = 2009, month = 12, day = 7)
				.formatDate(DateTextStyle.WEEKDAY_NUMERIC_DATE)
		)
	}

	@Test
	fun when_styleIsShortWeekdayNumericDate_then_readsTheShortWeekdayAndThePaddedNumbers() = runTuIndiceUiTest {
		assertDateTexts(
			"jue — 15/01/26" to millisOf().formatDate(DateTextStyle.SHORT_WEEKDAY_NUMERIC_DATE),
			"sáb — 17/01/26" to millisOf(day = 17).formatDate(DateTextStyle.SHORT_WEEKDAY_NUMERIC_DATE)
		)
	}

	@Test
	fun when_styleIsDayShortMonth_then_readsTheUnpaddedDayAndTheShortMonthWithItsPeriod() = runTuIndiceUiTest {
		assertDateTexts(
			"5 ene." to millisOf(month = 1, day = 5).formatDate(DateTextStyle.DAY_SHORT_MONTH),
			"22 sep." to millisOf(month = 9, day = 22).formatDate(DateTextStyle.DAY_SHORT_MONTH)
		)
	}

	@Test
	fun when_dateTextIsCapitalized_then_onlyAStyleThatStartsWithANameChanges() = runTuIndiceUiTest {
		assertDateTexts(
			"Jueves, 03:05 p. m." to UiText.Capitalized(millisOf().formatDate(DateTextStyle.WEEKDAY_TIME)),
			"Jueves — 15/01/26" to UiText.Capitalized(millisOf().formatDate(DateTextStyle.WEEKDAY_NUMERIC_DATE)),
			"Jue — 15/01/26" to UiText.Capitalized(
				millisOf().formatDate(DateTextStyle.SHORT_WEEKDAY_NUMERIC_DATE)
			),
			"Hoy, 03:05 p. m." to UiText.Capitalized(millisOf().formatDate(DateTextStyle.TODAY_TIME)),
			"15 de enero 2026" to UiText.Capitalized(millisOf().formatDate(DateTextStyle.DAY_MONTH_YEAR))
		)
	}

	@Test
	fun when_styleIsTodayAtClock_then_readsTodayAndTheClockWithoutAZeroBeforeTheHour() = runTuIndiceUiTest {
		assertDateTexts(
			"hoy a las 3:05 p. m." to millisOf(hour = 15, minute = 5).formatDate(DateTextStyle.TODAY_AT_CLOCK),
			"hoy a las 9:07 a. m." to millisOf(hour = 9, minute = 7).formatDate(DateTextStyle.TODAY_AT_CLOCK),
			"hoy a las 12:30 a. m." to millisOf(hour = 0, minute = 30).formatDate(DateTextStyle.TODAY_AT_CLOCK),
			"hoy a las 12:00 p. m." to millisOf(hour = 12, minute = 0).formatDate(DateTextStyle.TODAY_AT_CLOCK)
		)
	}

	@Test
	fun when_styleIsWeekdayDayMonthAtClock_then_readsTheDayWithoutAZero() = runTuIndiceUiTest {
		assertDateTexts(
			"el viernes 23 de octubre, 6:53 a. m." to millisOf(month = 10, day = 23, hour = 6, minute = 53)
				.formatDate(DateTextStyle.WEEKDAY_DAY_MONTH_AT_CLOCK),
			"el jueves 5 de marzo, 9:07 a. m." to millisOf(month = 3, day = 5, hour = 9, minute = 7)
				.formatDate(DateTextStyle.WEEKDAY_DAY_MONTH_AT_CLOCK)
		)
	}

	@Test
	fun when_styleIsWeekdayDayMonthYearAtClock_then_readsTheYearAfterTheDay() = runTuIndiceUiTest {
		assertDateText(
			"el viernes 15 de enero 2027, 3:05 p. m.",
			millisOf(year = 2027),
			DateTextStyle.WEEKDAY_DAY_MONTH_YEAR_AT_CLOCK
		)
	}

	@Test
	fun when_cellStyleIsTodayClock_then_readsTodayAndTheClockWithoutAnArticle() = runTuIndiceUiTest {
		assertDateTexts(
			"hoy, 3:05 p. m." to millisOf(hour = 15, minute = 5).formatDateCell(DateCellStyle.TODAY_CLOCK),
			"hoy, 9:07 a. m." to millisOf(hour = 9, minute = 7).formatDateCell(DateCellStyle.TODAY_CLOCK)
		)
	}

	@Test
	fun when_cellStyleIsWeekdayDayMonthClock_then_readsTheDayWithoutAnArticleOrAZero() = runTuIndiceUiTest {
		assertDateTexts(
			"jueves 15 de enero, 3:05 p. m." to millisOf().formatDateCell(DateCellStyle.WEEKDAY_DAY_MONTH_CLOCK),
			"viernes 23 de octubre, 6:53 a. m." to millisOf(month = 10, day = 23, hour = 6, minute = 53)
				.formatDateCell(DateCellStyle.WEEKDAY_DAY_MONTH_CLOCK)
		)
	}

	@Test
	fun when_cellStyleIsWeekdayDayMonthYearClock_then_readsTheYearAfterTheDayWithoutAnArticle() = runTuIndiceUiTest {
		assertDateTexts(
			"viernes 15 de enero 2027, 3:05 p. m." to millisOf(year = 2027)
				.formatDateCell(DateCellStyle.WEEKDAY_DAY_MONTH_YEAR_CLOCK)
		)
	}

	private fun ComposeUiTest.assertDateText(expected: String, millis: Long, style: DateTextStyle) {
		assertDateTexts(expected to millis.formatDate(style))
	}

	private fun ComposeUiTest.assertDateTexts(vararg cases: Pair<String, UiText>) {
		setTuIndiceTestContent {
			cases.forEachIndexed { index, (_, text) ->
				Text(
					modifier = Modifier.testTag(resolvedTextTag(index)),
					text = text.asString()
				)
			}
		}

		cases.forEachIndexed { index, (expected, _) ->
			// Resources resolve asynchronously off Android: wait for the text, then read the node.
			waitUntil(timeoutMillis = RESOURCE_TIMEOUT_MILLIS) {
				onAllNodesWithText(expected).fetchSemanticsNodes().isNotEmpty()
			}

			onNodeWithTag(resolvedTextTag(index)).assertTextEquals(expected)
		}
	}

	private companion object {
		const val RESOURCE_TIMEOUT_MILLIS = 5_000L

		fun resolvedTextTag(index: Int) = "date_text_resolved_$index"

		// formatDate reads the instant in the zone of the device, so the instants are built in
		// that zone. The default is Thursday, January 15th 2026, 3:05 in the afternoon.
		fun millisOf(
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
	}
}
