package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class EvaluationCalendarContentUiTest {
	@Test
	fun when_monthNavigationButtonsTapped_then_invokesCallbacks() = runTuIndiceUiTest {
		var previousClicks = 0
		var nextClicks = 0

		setTuIndiceTestContent {
			EvaluationCalendarContent(
				displayedMonth = LocalDate(2026, 1, 1),
				selectedDate = LocalDate(2026, 1, 15),
				onPreviousMonthClick = { previousClicks++ },
				onNextMonthClick = { nextClicks++ },
				onDateSelected = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationCalendarContainer)
		assertNodeVisible(EvaluationsUiTags.EvaluationCalendarMonthLabel)
		assertNodeVisible(EvaluationsUiTags.EvaluationWeekdayHeaderRow)

		onNodeWithTag(EvaluationsUiTags.EvaluationCalendarPreviousMonthButton).performClick()
		onNodeWithTag(EvaluationsUiTags.EvaluationCalendarNextMonthButton).performClick()

		assertEquals(1, previousClicks)
		assertEquals(1, nextClicks)
	}

	@Test
	fun when_dayCellTapped_then_invokesDateSelectedCallback() = runTuIndiceUiTest {
		var selectedDate: LocalDate? = null

		setTuIndiceTestContent {
			EvaluationCalendarContent(
				displayedMonth = LocalDate(2026, 1, 1),
				selectedDate = null,
				onPreviousMonthClick = {},
				onNextMonthClick = {},
				onDateSelected = { date ->
					selectedDate = date
				}
			)
		}

		onNodeWithTag(EvaluationsUiTags.calendarDayCell(15)).performClick()

		assertEquals(LocalDate(2026, 1, 15), selectedDate)
	}
}
