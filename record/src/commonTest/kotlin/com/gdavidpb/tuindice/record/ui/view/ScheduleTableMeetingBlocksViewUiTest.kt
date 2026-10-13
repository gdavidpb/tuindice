package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.record.presentation.model.ScheduleCellItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.testing.MondayAtNine
import com.gdavidpb.tuindice.record.testing.markedWeek
import com.gdavidpb.tuindice.record.testing.textLayout
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ScheduleTableMeetingBlocksViewUiTest {
	@Test
	fun when_noEvaluationFallsOnTheMeeting_then_onlyItsBlocksAreShown() = runTuIndiceUiTest {
		val meeting = meetingOf(day = ScheduleDay.Monday)

		setTuIndiceTestContent {
			ScheduleTableMeetingBlocksView(
				attemptId = "a1",
				day = ScheduleDay.Monday,
				meeting = meeting,
				textColor = Color.Black
			)
		}

		onNodeWithText("1-2").assertIsDisplayed()
		assertNodeHidden(RecordUiTags.scheduleTableEvaluation("a1", ScheduleDay.Monday.code))
		assertNodeHidden(RecordUiTags.scheduleTableInProgress("a1", ScheduleDay.Monday.code))
	}

	@Test
	fun when_anEvaluationFallsOnTheMeeting_then_theIconIsShownWithTheBlocks() = runTuIndiceUiTest {
		val meeting = meetingOf(day = ScheduleDay.Wednesday)

		setTuIndiceTestContent {
			ScheduleTableMeetingBlocksView(
				attemptId = "a1",
				day = ScheduleDay.Wednesday,
				meeting = meeting,
				textColor = Color.Black
			)
		}

		onNodeWithText("3").assertIsDisplayed()
		assertNodeVisible(RecordUiTags.scheduleTableEvaluation("a1", ScheduleDay.Wednesday.code))
	}

	@Test
	fun when_theMeetingIsBeingTaught_then_itsBlocksAreBold_andCarryTheInProgressTag() = runTuIndiceUiTest {
		val meeting = meetingOf(day = ScheduleDay.Monday, inProgress = true)

		setTuIndiceTestContent {
			ScheduleTableMeetingBlocksView(
				attemptId = "a1",
				day = ScheduleDay.Monday,
				meeting = meeting,
				textColor = Color.Black
			)
		}

		val inProgressTag = RecordUiTags.scheduleTableInProgress("a1", ScheduleDay.Monday.code)

		onNodeWithTag(inProgressTag).assertTextEquals("1-2")
		assertEquals(FontWeight.Bold, onNodeWithTag(inProgressTag).textLayout().layoutInput.style.fontWeight)
		// This week has one on the Monday meeting as well (see meetingOf).
		assertNodeVisible(RecordUiTags.scheduleTableEvaluation("a1", ScheduleDay.Monday.code))
	}

	private fun meetingOf(day: ScheduleDay, inProgress: Boolean = false): ScheduleCellItem {
		return markedWeek(now = if (inProgress) MondayAtNine else null, onMonday = inProgress).table.rows
			.first { row -> row.attemptId == "a1" }
			.meetings
			.getValue(day)
			.single()
	}
}
