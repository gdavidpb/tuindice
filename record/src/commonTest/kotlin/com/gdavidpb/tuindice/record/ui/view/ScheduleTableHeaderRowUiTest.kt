package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ScheduleTableHeaderRowUiTest {
	@Test
	fun when_todayIsOneOfTheDays_then_everyDayIsNamed_andOnlyTodaysIsMarked() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleTableHeaderRow(days = Weekdays, today = ScheduleDay.Wednesday)
		}

		WeekdayLabels.forEach { label -> onNodeWithText(label).assertIsDisplayed() }
		// assertNodeVisible also proves the mark is on one header only.
		assertNodeVisible(RecordUiTags.ScheduleTodayHeader)
		onNodeWithTag(RecordUiTags.ScheduleTodayHeader)
			.assertTextEquals("Mié")
			.assertContentDescriptionEquals("miércoles, hoy")
	}

	@Test
	fun when_noDayIsToday_then_everyDayIsNamed_andNoneIsMarked() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleTableHeaderRow(days = Weekdays)
		}

		WeekdayLabels.forEach { label -> onNodeWithText(label).assertIsDisplayed() }
		assertNodeHidden(RecordUiTags.ScheduleTodayHeader)
	}

	@Test
	fun when_todayIsNotAmongTheDaysShown_then_noneIsMarked() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			// A Sunday on a week that only shows Monday to Friday.
			ScheduleTableHeaderRow(days = Weekdays, today = ScheduleDay.Sunday)
		}

		assertNodeHidden(RecordUiTags.ScheduleTodayHeader)
		onNodeWithText("Lun").assertIsDisplayed()
	}

	private companion object {
		val Weekdays = ScheduleDay.entries.filterNot(ScheduleDay::isWeekend)
		val WeekdayLabels = listOf("Lun", "Mar", "Mié", "Jue", "Vie")
	}
}
