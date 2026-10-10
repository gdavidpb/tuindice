package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.testing.MondayAtNine
import com.gdavidpb.tuindice.record.testing.clashingWeek
import com.gdavidpb.tuindice.record.testing.scheduleAttempt
import com.gdavidpb.tuindice.record.testing.scheduleEntry
import com.gdavidpb.tuindice.record.testing.scheduleItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ScheduleTableViewUiTest {
	@Test
	fun when_theTermHasSubjects_then_theWeekdaysHeadTheColumns_andEachSubjectHasItsRow() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleTableView(table = clashingWeek().table)
		}

		assertNodeVisible(RecordUiTags.ScheduleTable)
		WeekdayLabels.forEach { label -> onNodeWithText(label).assertIsDisplayed() }
		// Nothing meets on the weekend, so it has no column.
		onAllNodesWithText("Sáb").assertCountEquals(0)
		onAllNodesWithText("Dom").assertCountEquals(0)
		assertNodeVisible(RecordUiTags.scheduleTableRow("a1"))
		assertNodeVisible(RecordUiTags.scheduleTableRow("a2"))
		assertNodeVisible(RecordUiTags.scheduleTableRow("a3"))
		onNode(hasContentDescription("CI5437, sección 2, lunes bloques 2 a 3")).assertIsDisplayed()
		// No moment was given: no day is today and no class is in progress.
		assertNodeHidden(RecordUiTags.ScheduleTodayHeader)
		assertNodeHidden(RecordUiTags.scheduleTableInProgress("a1", ScheduleDay.Monday.code), useUnmergedTree = true)
	}

	@Test
	fun when_somethingMeetsOnSaturday_then_thatDayGainsAColumn() = runTuIndiceUiTest {
		val table = scheduleItem(
			scheduleAttempt(
				id = "a1",
				code = "FS2211",
				schedule = listOf(scheduleEntry(day = ScheduleDay.Saturday, blocks = 1..2))
			)
		).table

		setTuIndiceTestContent {
			ScheduleTableView(table = table)
		}

		onNodeWithText("Sáb").assertIsDisplayed()
		onNodeWithText("1-2").assertIsDisplayed()
		assertNodeVisible(RecordUiTags.scheduleTableCell("a1", ScheduleDay.Saturday.code), useUnmergedTree = true)
		onAllNodesWithText("Dom").assertCountEquals(0)
	}

	@Test
	fun when_aClassIsBeingTaught_then_todayHeadsItsColumn_andTheRowSaysItIsInProgress() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleTableView(table = clashingWeek(now = MondayAtNine).table)
		}

		assertNodeVisible(RecordUiTags.ScheduleTodayHeader)
		onNode(hasContentDescription("lunes, hoy")).assertIsDisplayed()
		// Block 2 is the one being taught: both Monday meetings hold it, Wednesday's does not.
		assertNodeVisible(RecordUiTags.scheduleTableInProgress("a1", ScheduleDay.Monday.code), useUnmergedTree = true)
		assertNodeVisible(RecordUiTags.scheduleTableInProgress("a2", ScheduleDay.Monday.code), useUnmergedTree = true)
		assertNodeHidden(RecordUiTags.scheduleTableInProgress("a1", ScheduleDay.Wednesday.code), useUnmergedTree = true)
		onNode(
			hasContentDescription("CI5311, sección 1, aula MYS-116, lunes bloques 1 a 2, en curso, miércoles bloque 3")
		).assertIsDisplayed()
		onNode(hasContentDescription("CI5437, sección 2, lunes bloques 2 a 3, en curso")).assertIsDisplayed()
	}

	@Test
	fun when_theTableIsWiderThanItsSpace_then_itScrollsSideways_insteadOfSqueezingTheDays() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleTableView(
				modifier = Modifier.width(200.dp),
				table = clashingWeek().table
			)
		}

		onNodeWithText("Lun").assertIsDisplayed()
		onNodeWithText("Vie").assertIsNotDisplayed()

		repeat(times = 2) {
			onNodeWithTag(RecordUiTags.ScheduleTable).performTouchInput { swipeLeft() }
		}

		onNodeWithText("Vie").assertIsDisplayed()
	}

	private companion object {
		val WeekdayLabels = listOf("Lun", "Mar", "Mié", "Jue", "Vie")
	}
}
