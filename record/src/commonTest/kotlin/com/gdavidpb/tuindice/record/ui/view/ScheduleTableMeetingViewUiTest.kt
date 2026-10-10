package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.record.domain.model.ScheduleNow
import com.gdavidpb.tuindice.record.presentation.model.ScheduleCellItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.testing.MondayAtNine
import com.gdavidpb.tuindice.record.testing.clashingWeek
import com.gdavidpb.tuindice.record.testing.scheduleAttempt
import com.gdavidpb.tuindice.record.testing.scheduleEntry
import com.gdavidpb.tuindice.record.testing.scheduleItem
import com.gdavidpb.tuindice.record.testing.textLayout
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalTestApi::class)
class ScheduleTableMeetingViewUiTest {
	@Test
	fun when_theMeetingStandsAlone_then_itShowsItsBlocks_andCarriesNoMark() = runTuIndiceUiTest {
		val meeting = meetingOf(attemptId = "a1", day = ScheduleDay.Wednesday)

		setTuIndiceTestContent {
			ScheduleTableMeetingView(attemptId = "a1", day = ScheduleDay.Wednesday, meeting = meeting)
		}

		onNodeWithText("3").assertIsDisplayed()
		assertNotEquals(FontWeight.Bold, onNodeWithText("3").textLayout().layoutInput.style.fontWeight)
		assertNodeHidden(RecordUiTags.scheduleTableClash("a1", ScheduleDay.Wednesday.code))
		assertNodeHidden(RecordUiTags.scheduleTableInProgress("a1", ScheduleDay.Wednesday.code))
	}

	@Test
	fun when_theMeetingOverlapsAnotherSubjects_then_itCarriesTheClashTag_andNoOther() = runTuIndiceUiTest {
		val meeting = meetingOf(attemptId = "a1", day = ScheduleDay.Monday)

		setTuIndiceTestContent {
			ScheduleTableMeetingView(attemptId = "a1", day = ScheduleDay.Monday, meeting = meeting)
		}

		assertNodeVisible(RecordUiTags.scheduleTableClash("a1", ScheduleDay.Monday.code))
		onNodeWithText("1-2").assertIsDisplayed()
		assertNodeHidden(RecordUiTags.scheduleTableInProgress("a1", ScheduleDay.Monday.code))
	}

	@Test
	fun when_theMeetingIsBeingTaught_then_itsBlocksCarryTheInProgressTag_inBold() = runTuIndiceUiTest {
		// The same Monday meeting with nothing overlapping it, looked at halfway through block 2.
		val meeting = scheduleItem(
			scheduleAttempt(
				id = "a1",
				code = "CI5311",
				schedule = listOf(scheduleEntry(day = ScheduleDay.Monday, blocks = 1..2))
			),
			now = MondayAtNine
		).table.rows.single().meetings.getValue(ScheduleDay.Monday).single()

		setTuIndiceTestContent {
			ScheduleTableMeetingView(attemptId = "a1", day = ScheduleDay.Monday, meeting = meeting)
		}

		val inProgressTag = RecordUiTags.scheduleTableInProgress("a1", ScheduleDay.Monday.code)

		assertNodeVisible(inProgressTag)
		onNodeWithTag(inProgressTag).assertTextEquals("1-2")
		assertEquals(FontWeight.Bold, onNodeWithTag(inProgressTag).textLayout().layoutInput.style.fontWeight)
		assertNodeHidden(RecordUiTags.scheduleTableClash("a1", ScheduleDay.Monday.code))
	}

	@Test
	fun when_theMeetingIsBeingTaughtAndOverlapsAnother_then_itAnswersToBothTags() = runTuIndiceUiTest {
		val meeting = meetingOf(attemptId = "a1", day = ScheduleDay.Monday, now = MondayAtNine)

		setTuIndiceTestContent {
			ScheduleTableMeetingView(attemptId = "a1", day = ScheduleDay.Monday, meeting = meeting)
		}

		val clashTag = RecordUiTags.scheduleTableClash("a1", ScheduleDay.Monday.code)
		val inProgressTag = RecordUiTags.scheduleTableInProgress("a1", ScheduleDay.Monday.code)

		// Each mark keeps its own tag: the outline on the surface, "in progress" on the blocks in it.
		assertNodeVisible(clashTag)
		assertNodeVisible(inProgressTag)
		onNodeWithTag(inProgressTag)
			.assertTextEquals("1-2")
			.assert(hasAnyAncestor(hasTestTag(clashTag)))
	}

	private fun meetingOf(attemptId: String, day: ScheduleDay, now: ScheduleNow? = null): ScheduleCellItem {
		return clashingWeek(now = now).table.rows
			.first { row -> row.attemptId == attemptId }
			.meetings
			.getValue(day)
			.single()
	}
}
