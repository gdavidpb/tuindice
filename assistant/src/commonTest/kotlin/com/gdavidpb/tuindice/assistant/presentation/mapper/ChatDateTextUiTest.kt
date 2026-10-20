package com.gdavidpb.tuindice.assistant.presentation.mapper

import androidx.compose.material3.Text
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test

// What the chat says of a moment: "hoy a las" and the clock when it is the day the moment is read, the weekday, the day
// and the month on another day of the year, and the year after the day on another year. The clock has no zero before
// its hour, as the class schedules have.
@OptIn(ExperimentalTestApi::class)
class ChatDateTextUiTest {
	@Test
	fun when_theMomentIsOnTheDayItIsRead_then_itSaysTodayAndTheClock() = runTuIndiceUiTest {
		assertChatDate(
			expected = "hoy a las 3:05 p. m.",
			millis = millisOf(hour = 15, minute = 5),
			nowMillis = millisOf(hour = 9)
		)
	}

	@Test
	fun when_theMomentIsAnotherDayOfTheYear_then_itSaysTheWeekdayTheDayTheMonthAndTheClock() = runTuIndiceUiTest {
		assertChatDate(
			expected = "el viernes 23 de octubre, 6:53 a. m.",
			millis = millisOf(month = 10, day = 23, hour = 6, minute = 53),
			nowMillis = millisOf(month = 10, day = 18, hour = 12)
		)
	}

	@Test
	fun when_theMomentIsInAnotherYear_then_itSaysTheYearAfterTheDay() = runTuIndiceUiTest {
		assertChatDate(
			expected = "el viernes 15 de enero 2027, 3:05 p. m.",
			millis = millisOf(year = 2027, hour = 15, minute = 5),
			nowMillis = millisOf(hour = 12)
		)
	}

	@Test
	fun when_aCellMomentIsOnTheDayItIsRead_then_itSaysTodayAndTheClockWithoutAnArticle() = runTuIndiceUiTest {
		assertChatCellDate(
			expected = "hoy, 3:05 p. m.",
			millis = millisOf(hour = 15, minute = 5),
			nowMillis = millisOf(hour = 9)
		)
	}

	@Test
	fun when_aCellMomentIsAnotherDay_then_itSaysTheWeekdayAndTheDayWithoutAnArticle() = runTuIndiceUiTest {
		assertChatCellDate(
			expected = "viernes 23 de octubre, 6:53 a. m.",
			millis = millisOf(month = 10, day = 23, hour = 6, minute = 53),
			nowMillis = millisOf(month = 10, day = 18, hour = 12)
		)
	}

	@Test
	fun when_aCellMomentIsInAnotherYear_then_itSaysTheYearAfterTheDayWithoutAnArticle() = runTuIndiceUiTest {
		assertChatCellDate(
			expected = "viernes 15 de enero 2027, 3:05 p. m.",
			millis = millisOf(year = 2027, hour = 15, minute = 5),
			nowMillis = millisOf(hour = 12)
		)
	}

	private fun ComposeUiTest.assertChatCellDate(expected: String, millis: Long, nowMillis: Long) {
		setTuIndiceTestContent {
			Text(text = millis.toChatCellDateText(nowMillis).asString())
		}

		onNodeWithText(expected).assertIsDisplayed()
	}

	private fun ComposeUiTest.assertChatDate(expected: String, millis: Long, nowMillis: Long) {
		setTuIndiceTestContent {
			Text(text = millis.toChatDateText(nowMillis).asString())
		}

		onNodeWithText(expected).assertIsDisplayed()
	}

	// The instants are built in the zone of the device, as the chat reads them. The default is 15 January 2026.
	private fun millisOf(
		year: Int = 2026,
		month: Int = 1,
		day: Int = 15,
		hour: Int = 15,
		minute: Int = 5
	): Long =
		LocalDateTime(year = year, month = month, day = day, hour = hour, minute = minute)
			.toInstant(TimeZone.currentSystemDefault())
			.toEpochMilliseconds()
}
