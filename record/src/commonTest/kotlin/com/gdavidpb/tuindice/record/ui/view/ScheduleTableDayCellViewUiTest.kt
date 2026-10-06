package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.record.presentation.model.ScheduleCellItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
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
class ScheduleTableDayCellViewUiTest {
	@Test
	fun when_theSubjectMeetsTwiceThatDay_then_eachMeetingHasItsLineOfBlocks() = runTuIndiceUiTest {
		val meetings = mondayMeetings()

		setTuIndiceTestContent {
			ScheduleTableDayCellView(attemptId = "a1", day = ScheduleDay.Monday, meetings = meetings)
		}

		assertNodeVisible(RecordUiTags.scheduleTableCell("a1", ScheduleDay.Monday.code))
		// A range for the meeting of two blocks, the block alone for the meeting of one.
		onNodeWithText("1-2").assertIsDisplayed()
		onNodeWithText("4").assertIsDisplayed()
	}

	@Test
	fun when_oneOfTheMeetingsOverlapsAnotherSubjects_then_onlyThatMeetingIsMarked() = runTuIndiceUiTest {
		val meetings = mondayMeetings()

		setTuIndiceTestContent {
			ScheduleTableDayCellView(attemptId = "a1", day = ScheduleDay.Monday, meetings = meetings)
		}

		// assertNodeVisible also proves the mark is on one meeting only: the other has no clash.
		assertNodeVisible(RecordUiTags.scheduleTableClash("a1", ScheduleDay.Monday.code))
		onNodeWithTag(RecordUiTags.scheduleTableClash("a1", ScheduleDay.Monday.code))
			.assert(hasAnyDescendant(hasText("1-2")))
		assertNodeHidden(RecordUiTags.scheduleTableInProgress("a1", ScheduleDay.Monday.code))
	}

	@Test
	fun when_theSubjectDoesNotMeetThatDay_then_theCellStaysEmpty() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleTableDayCellView(attemptId = "a1", day = ScheduleDay.Tuesday, meetings = emptyList())
		}

		// The cell keeps its place in the row, with nothing written in it.
		onNodeWithTag(RecordUiTags.scheduleTableCell("a1", ScheduleDay.Tuesday.code)).assertExists()
		onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text)).assertCountEquals(0)
		assertNodeHidden(RecordUiTags.scheduleTableClash("a1", ScheduleDay.Tuesday.code))
	}

	// CI5311 meets twice on Monday, blocks 1-2 and block 4; CI5437 overlaps only the first one.
	private fun mondayMeetings(): List<ScheduleCellItem> = scheduleItem(
		scheduleAttempt(
			id = "a1",
			code = "CI5311",
			schedule = listOf(
				scheduleEntry(day = ScheduleDay.Monday, blocks = 1..2),
				scheduleEntry(day = ScheduleDay.Monday, blocks = 4..4)
			)
		),
		scheduleAttempt(
			id = "a2",
			code = "CI5437",
			schedule = listOf(scheduleEntry(day = ScheduleDay.Monday, blocks = 2..3))
		)
	).table.rows.first().meetings.getValue(ScheduleDay.Monday)
}
