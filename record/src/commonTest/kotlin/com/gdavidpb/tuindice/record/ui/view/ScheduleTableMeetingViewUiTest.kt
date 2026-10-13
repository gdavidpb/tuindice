package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.base.domain.model.ScheduleNow
import com.gdavidpb.tuindice.record.presentation.model.ScheduleCellItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.testing.MondayAtNine
import com.gdavidpb.tuindice.record.testing.ScheduleTestDate
import com.gdavidpb.tuindice.record.testing.clashingWeek
import com.gdavidpb.tuindice.record.testing.markedWeek
import com.gdavidpb.tuindice.record.testing.scheduleAttempt
import com.gdavidpb.tuindice.record.testing.scheduleEntry
import com.gdavidpb.tuindice.record.testing.scheduleItem
import com.gdavidpb.tuindice.record.testing.textLayout
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ScheduleTableMeetingViewUiTest {
	@Test
	fun when_theMeetingStandsAlone_then_itShowsItsBlocks_andCarriesNoMark() = runTuIndiceUiTest {
		val meeting = meetingOf(attemptId = "a1", day = ScheduleDay.Wednesday)

		setTuIndiceTestContent {
			ScheduleTableMeetingView(attemptId = "a1", day = ScheduleDay.Wednesday, meeting = meeting, onEvaluationClick = {})
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
			ScheduleTableMeetingView(attemptId = "a1", day = ScheduleDay.Monday, meeting = meeting, onEvaluationClick = {})
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
			ScheduleTableMeetingView(attemptId = "a1", day = ScheduleDay.Monday, meeting = meeting, onEvaluationClick = {})
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
			ScheduleTableMeetingView(attemptId = "a1", day = ScheduleDay.Monday, meeting = meeting, onEvaluationClick = {})
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

	@Test
	fun when_noEvaluationFallsOnTheMeeting_then_itHasNoIcon_andIsNotAButton() = runTuIndiceUiTest {
		val meeting = meetingOf(attemptId = "a1", day = ScheduleDay.Wednesday)

		setTuIndiceTestContent {
			ScheduleTableMeetingView(
				attemptId = "a1",
				day = ScheduleDay.Wednesday,
				meeting = meeting,
				onEvaluationClick = {}
			)
		}

		assertNodeHidden(RecordUiTags.scheduleTableEvaluation("a1", ScheduleDay.Wednesday.code), useUnmergedTree = true)
		onNodeWithText("3").assertHasNoClickAction()
	}

	@Test
	fun when_anEvaluationFallsOnTheMeeting_then_itCarriesTheIconUnderItsBlocks() = runTuIndiceUiTest {
		val meeting = markedMeeting(ScheduleDay.Wednesday)

		setTuIndiceTestContent {
			ScheduleTableMeetingView(
				attemptId = "a1",
				day = ScheduleDay.Wednesday,
				meeting = meeting,
				onEvaluationClick = {}
			)
		}

		val icon = onNodeWithTag(
			RecordUiTags.scheduleTableEvaluationIcon("a1", ScheduleDay.Wednesday.code),
			useUnmergedTree = true
		)

		icon.assertIsDisplayed()
		// Under the blocks, not beside them.
		assertTrue(
			icon.fetchSemanticsNode().positionInRoot.y >
				onNodeWithText("3", useUnmergedTree = true).fetchSemanticsNode().positionInRoot.y
		)
	}

	@Test
	fun when_anEvaluationFallsOnTheMeeting_then_itIsAButtonThatAsksForThatWeek() = runTuIndiceUiTest {
		val meeting = markedMeeting(ScheduleDay.Wednesday)
		var asked: LocalDate? = null

		setTuIndiceTestContent {
			ScheduleTableMeetingView(
				attemptId = "a1",
				day = ScheduleDay.Wednesday,
				meeting = meeting,
				onEvaluationClick = { weekStart -> asked = weekStart }
			)
		}

		val button = onNodeWithText("3")

		button.assertHasClickAction()
		button.assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
		// What a screen reader announces for the action, besides the role.
		button.assert(
			SemanticsMatcher("announces what it does") { node ->
				node.config.getOrNull(SemanticsActions.OnClick)?.label == "Ver las evaluaciones de la semana"
			}
		)

		button.performClick()

		assertEquals(ScheduleTestDate, asked)
	}

	@Test
	fun when_anEvaluationFallsOnTheMeeting_then_theButtonCarriesItsOwnTag() = runTuIndiceUiTest {
		val meeting = markedMeeting(ScheduleDay.Wednesday)

		setTuIndiceTestContent {
			ScheduleTableMeetingView(
				attemptId = "a1",
				day = ScheduleDay.Wednesday,
				meeting = meeting,
				onEvaluationClick = {}
			)
		}

		// The tag is on the node that is clicked, not on the decorative icon inside it.
		onNodeWithTag(RecordUiTags.scheduleTableEvaluation("a1", ScheduleDay.Wednesday.code))
			.assertHasClickAction()
	}

	@Test
	fun when_anEvaluationFallsOnTheMeeting_then_theButtonSaysAllOfItselfNotJustItsBlocks() = runTuIndiceUiTest {
		val meeting = markedMeeting(ScheduleDay.Wednesday)

		setTuIndiceTestContent {
			ScheduleTableMeetingView(
				attemptId = "a1",
				day = ScheduleDay.Wednesday,
				meeting = meeting,
				onEvaluationClick = {}
			)
		}

		// Being a button it leaves the row's merged description, so a screen reader would stop on "3"
		// alone: it says the day, the blocks and the evaluation, the words the week grid's block uses.
		onNode(hasContentDescription("CI5311, miércoles, bloque 3, aula MYS-116, con evaluación ese día"))
			.assertHasClickAction()
		onNodeWithText("3").assertHasClickAction()
	}

	@Test
	fun when_theMarkedMeetingClashesAndIsBeingTaught_then_itKeepsBothMarks_andTheIcon() = runTuIndiceUiTest {
		val meeting = markedWeekMeeting(day = ScheduleDay.Monday, now = MondayAtNine)

		setTuIndiceTestContent {
			ScheduleTableMeetingView(
				attemptId = "a1",
				day = ScheduleDay.Monday,
				meeting = meeting,
				onEvaluationClick = {}
			)
		}

		// The cell is a button, so its descendants are merged into it: the tags inside are read from the
		// unmerged tree. The button answers to its own tag and the marks to theirs.
		assertNodeVisible(RecordUiTags.scheduleTableEvaluation("a1", ScheduleDay.Monday.code))
		assertNodeVisible(RecordUiTags.scheduleTableClash("a1", ScheduleDay.Monday.code), useUnmergedTree = true)
		assertNodeVisible(RecordUiTags.scheduleTableInProgress("a1", ScheduleDay.Monday.code), useUnmergedTree = true)
		assertNodeVisible(RecordUiTags.scheduleTableEvaluationIcon("a1", ScheduleDay.Monday.code), useUnmergedTree = true)
		onNodeWithTag(RecordUiTags.scheduleTableInProgress("a1", ScheduleDay.Monday.code), useUnmergedTree = true)
			.assertTextEquals("1-2")
	}

	private fun markedMeeting(day: ScheduleDay): ScheduleCellItem = markedWeekMeeting(day = day, now = null)

	private fun markedWeekMeeting(day: ScheduleDay, now: ScheduleNow?): ScheduleCellItem {
		return markedWeek(now = now, onMonday = true).table.rows
			.first { row -> row.attemptId == "a1" }
			.meetings
			.getValue(day)
			.single()
	}

	private fun meetingOf(attemptId: String, day: ScheduleDay, now: ScheduleNow? = null): ScheduleCellItem {
		return clashingWeek(now = now).table.rows
			.first { row -> row.attemptId == attemptId }
			.meetings
			.getValue(day)
			.single()
	}
}
