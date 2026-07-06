package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class CalendarDayCellUiTest {
	@Test
	fun when_dayTapped_then_invokesDateSelectionCallback() = runTuIndiceUiTest {
		val date = LocalDate(2026, 1, 15)
		var selectedDate: LocalDate? = null

		setTuIndiceTestContent {
			CalendarDayCell(
				date = date,
				selectedDate = null,
				today = LocalDate(2026, 1, 10),
				onDateSelected = { selected -> selectedDate = selected }
			)
		}

		onNodeWithText("15").assertIsDisplayed()
		onNodeWithTag(EvaluationsUiTags.calendarDayCell(15)).performClick()

		assertEquals(date, selectedDate)
	}

	@Test
	fun when_dayIsNotSelectable_then_isDisabledAndIgnoresTaps() = runTuIndiceUiTest {
		val date = LocalDate(2026, 5, 20)
		var selectedDate: LocalDate? = null

		setTuIndiceTestContent {
			CalendarDayCell(
				date = date,
				selectedDate = null,
				today = LocalDate(2026, 1, 10),
				isSelectable = false,
				onDateSelected = { selected -> selectedDate = selected }
			)
		}

		onNodeWithText("20").assertIsDisplayed()
		onNodeWithTag(EvaluationsUiTags.calendarDayCell(20)).assertIsNotEnabled()
		onNodeWithTag(EvaluationsUiTags.calendarDayCell(20)).performClick()

		assertEquals(null, selectedDate)
	}

	@Test
	fun when_dateIsNull_then_rendersPlaceholderWithoutTextAndIsDisabled() = runTuIndiceUiTest {
		var selectedDate: LocalDate? = null

		setTuIndiceTestContent {
			CalendarDayCell(
				date = null,
				selectedDate = null,
				today = LocalDate(2026, 1, 10),
				onDateSelected = { selected -> selectedDate = selected }
			)
		}

		assertNodeVisible("evaluation_calendar_day_placeholder")
		onNodeWithTag("evaluation_calendar_day_placeholder").assertIsNotEnabled()
		assertEquals(null, selectedDate)
	}
}
