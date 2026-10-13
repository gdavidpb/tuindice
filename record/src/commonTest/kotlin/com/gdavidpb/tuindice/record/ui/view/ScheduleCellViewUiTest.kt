package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.base.domain.model.ScheduleNow
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.record.presentation.model.ScheduleCellItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.testing.MondayAtNine
import com.gdavidpb.tuindice.record.testing.ScheduleTestDate
import com.gdavidpb.tuindice.record.testing.assertDpEquals
import com.gdavidpb.tuindice.record.testing.clashingWeek
import com.gdavidpb.tuindice.record.testing.markedItem
import com.gdavidpb.tuindice.record.testing.markedWeek
import com.gdavidpb.tuindice.record.testing.scheduleAttempt
import com.gdavidpb.tuindice.record.testing.scheduleEntry
import com.gdavidpb.tuindice.record.testing.scheduleEvaluation
import com.gdavidpb.tuindice.record.testing.scheduleItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.ScheduleGridDefaults
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ScheduleCellViewUiTest {
	@Test
	fun when_theMeetingSpansBlocksInARoom_then_theCellShowsCodeAndRoom_andSaysWhenItMeets() = runTuIndiceUiTest {
		// Alone on its day, so nothing but the meeting itself is said.
		val cell = scheduleItem(
			scheduleAttempt(
				id = "a1",
				code = "CI5311",
				schedule = listOf(scheduleEntry(day = ScheduleDay.Tuesday, blocks = 3..4, classroom = "MYS-116"))
			)
		).grid.days.first { dayItem -> dayItem.day == ScheduleDay.Tuesday }.cells.single()

		setTuIndiceTestContent {
			ScheduleCellView(day = ScheduleDay.Tuesday, cell = cell, onEvaluationClick = {})
		}

		val tag = RecordUiTags.scheduleCell("a1", ScheduleDay.Tuesday.code, 3)

		assertNodeVisible(tag)
		onNodeWithTag(tag)
			.assertTextEquals("CI5311", "MYS-116")
			.assertContentDescriptionEquals("CI5311, martes, bloques 3 a 4, aula MYS-116")
	}

	@Test
	fun when_theMeetingIsASingleBlock_then_theRoomIsSaid_butNotDrawn() = runTuIndiceUiTest {
		val cell = cellOf(attemptId = "a1", day = ScheduleDay.Wednesday)

		setTuIndiceTestContent {
			ScheduleCellView(day = ScheduleDay.Wednesday, cell = cell, onEvaluationClick = {})
		}

		// One block is one line tall: only the code fits, and the room stays in what is read aloud.
		onNodeWithTag(RecordUiTags.scheduleCell("a1", ScheduleDay.Wednesday.code, 3))
			.assertTextEquals("CI5311")
			.assertContentDescriptionEquals("CI5311, miércoles, bloque 3, aula MYS-116")
		onAllNodesWithText("MYS-116").assertCountEquals(0)
	}

	@Test
	fun when_theMeetingOverlapsAnotherSubjects_then_whatIsSaidEndsInTheClash() = runTuIndiceUiTest {
		val cell = cellOf(attemptId = "a2", day = ScheduleDay.Monday)

		setTuIndiceTestContent {
			ScheduleCellView(day = ScheduleDay.Monday, cell = cell, onEvaluationClick = {})
		}

		// The outline is all the eye gets, so the words are only in what is read aloud. No room
		// was named for this subject, so none is said.
		onNodeWithTag(RecordUiTags.scheduleCell("a2", ScheduleDay.Monday.code, 2))
			.assertTextEquals("CI5437")
			.assertContentDescriptionEquals("CI5437, lunes, bloques 2 a 3. Choque de horario")
		onAllNodesWithText("Choque de horario").assertCountEquals(0)
	}

	@Test
	fun when_theMeetingIsBeingTaught_then_whatIsSaidEndsInEnCurso_afterTheClash() = runTuIndiceUiTest {
		val cell = cellOf(attemptId = "a1", day = ScheduleDay.Monday, now = MondayAtNine)

		setTuIndiceTestContent {
			ScheduleCellView(day = ScheduleDay.Monday, cell = cell, onEvaluationClick = {})
		}

		onNodeWithTag(RecordUiTags.scheduleCell("a1", ScheduleDay.Monday.code, 1))
			.assertTextEquals("CI5311", "MYS-116")
			.assertContentDescriptionEquals("CI5311, lunes, bloques 1 a 2, aula MYS-116. Choque de horario. En curso")
	}

	@Test
	fun when_noEvaluationFallsOnTheMeeting_then_itHasNoIcon_andIsNotAButton() = runTuIndiceUiTest {
		val cell = cellOf(attemptId = "a1", day = ScheduleDay.Wednesday)

		setTuIndiceTestContent {
			ScheduleCellView(day = ScheduleDay.Wednesday, cell = cell, onEvaluationClick = {})
		}

		assertNodeHidden(RecordUiTags.scheduleCellEvaluation("a1", ScheduleDay.Wednesday.code, 3), useUnmergedTree = true)
		onNodeWithTag(RecordUiTags.scheduleCell("a1", ScheduleDay.Wednesday.code, 3)).assertHasNoClickAction()
	}

	@Test
	fun when_anEvaluationFallsOnASingleBlockMeeting_then_theIconSitsAtTheBottomEnd_clearOfTheCode() = runTuIndiceUiTest {
		val cell = markedCell(attemptId = "a1", day = ScheduleDay.Wednesday)

		setTuIndiceTestContent {
			ScheduleCellView(
				modifier = Modifier.cellSizeOf(cell),
				day = ScheduleDay.Wednesday,
				cell = cell,
				onEvaluationClick = {}
			)
		}

		val bounds = onNodeWithTag(RecordUiTags.scheduleCell("a1", ScheduleDay.Wednesday.code, 3)).getUnclippedBoundsInRoot()
		val icon = onNodeWithTag(
			RecordUiTags.scheduleCellEvaluation("a1", ScheduleDay.Wednesday.code, 3),
			useUnmergedTree = true
		).getUnclippedBoundsInRoot()
		val code = onNodeWithText("CI5311", useUnmergedTree = true).getUnclippedBoundsInRoot()

		// The icon is the token's size, inside the cell and at its bottom end.
		assertDpEquals(TuIndiceComponentSizes.IconSmall, icon.right - icon.left, "width of the icon")
		assertDpEquals(TuIndiceComponentSizes.IconSmall, icon.bottom - icon.top, "height of the icon")
		assertTrue(icon.right < bounds.right && icon.bottom < bounds.bottom)
		assertTrue(icon.left > bounds.left && icon.top > bounds.top)
		assertTrue(icon.left > bounds.right / 2 && icon.bottom > bounds.bottom / 2)
		// A code of six characters, the length the university's are, stops before the icon starts.
		assertTrue(code.right <= icon.left, "the code ends at ${code.right} and the icon starts at ${icon.left}")
	}

	@Test
	fun when_anEvaluationFallsOnALongMeetingWithARoom_then_theIconSitsBelowBothLines() = runTuIndiceUiTest {
		val cell = markedCell(
			attemptId = "a1",
			day = ScheduleDay.Tuesday,
			attempt = scheduleAttempt(
				id = "a1",
				code = "CI5311",
				schedule = listOf(scheduleEntry(day = ScheduleDay.Tuesday, blocks = 3..4, classroom = "MYS-116"))
			)
		)

		setTuIndiceTestContent {
			ScheduleCellView(
				modifier = Modifier.cellSizeOf(cell),
				day = ScheduleDay.Tuesday,
				cell = cell,
				onEvaluationClick = {}
			)
		}

		val icon = onNodeWithTag(
			RecordUiTags.scheduleCellEvaluation("a1", ScheduleDay.Tuesday.code, 3),
			useUnmergedTree = true
		).getUnclippedBoundsInRoot()
		val code = onNodeWithText("CI5311", useUnmergedTree = true).getUnclippedBoundsInRoot()
		val room = onNodeWithText("MYS-116", useUnmergedTree = true).getUnclippedBoundsInRoot()

		assertTrue(icon.top >= code.bottom, "the icon starts at ${icon.top} and the code ends at ${code.bottom}")
		assertTrue(icon.top >= room.bottom, "the icon starts at ${icon.top} and the room ends at ${room.bottom}")
	}

	@Test
	fun when_anEvaluationFallsOnTheMeeting_then_whatIsSaidAddsIt_beforeTheOtherMarks() = runTuIndiceUiTest {
		val cell = markedCell(attemptId = "a1", day = ScheduleDay.Wednesday)

		setTuIndiceTestContent {
			ScheduleCellView(day = ScheduleDay.Wednesday, cell = cell, onEvaluationClick = {})
		}

		onNodeWithTag(RecordUiTags.scheduleCell("a1", ScheduleDay.Wednesday.code, 3))
			.assertContentDescriptionEquals("CI5311, miércoles, bloque 3, aula MYS-116, con evaluación ese día")
	}

	@Test
	fun when_theMarkedMeetingClashesAndIsBeingTaught_then_itSaysAllThree_andKeepsTheIcon() = runTuIndiceUiTest {
		val cell = markedWeek(now = MondayAtNine, onMonday = true).grid.days
			.first { dayItem -> dayItem.day == ScheduleDay.Monday }
			.cells
			.single { it.attemptId == "a1" }

		setTuIndiceTestContent {
			ScheduleCellView(
				modifier = Modifier.cellSizeOf(cell),
				day = ScheduleDay.Monday,
				cell = cell,
				onEvaluationClick = {}
			)
		}

		onNodeWithTag(RecordUiTags.scheduleCell("a1", ScheduleDay.Monday.code, 1))
			.assertTextEquals("CI5311", "MYS-116")
			.assertContentDescriptionEquals(
				"CI5311, lunes, bloques 1 a 2, aula MYS-116, con evaluación ese día. Choque de horario. En curso"
			)
		assertNodeVisible(RecordUiTags.scheduleCellEvaluation("a1", ScheduleDay.Monday.code, 1), useUnmergedTree = true)
	}

	@Test
	fun when_anEvaluationFallsOnTheMeeting_then_itIsAButtonThatAsksForThatWeek() = runTuIndiceUiTest {
		val cell = markedCell(attemptId = "a1", day = ScheduleDay.Wednesday)
		var asked: LocalDate? = null

		setTuIndiceTestContent {
			ScheduleCellView(
				day = ScheduleDay.Wednesday,
				cell = cell,
				onEvaluationClick = { weekStart -> asked = weekStart }
			)
		}

		val button = onNodeWithTag(RecordUiTags.scheduleCell("a1", ScheduleDay.Wednesday.code, 3))

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

	// The meeting of [attemptId] on [day] in the week of the other tests, with an evaluation of CI5311
	// on its Wednesday meeting; [attempt] draws another subject's schedule instead.
	private fun markedCell(
		attemptId: String,
		day: ScheduleDay,
		attempt: AttemptProjection? = null
	): ScheduleCellItem {
		val item = if (attempt == null) {
			markedWeek()
		} else {
			markedItem(
				attempt,
				evaluations = listOf(scheduleEvaluation(attemptId = attempt.id, date = LocalDate(2026, 10, 6)))
			)
		}

		return item.grid.days.first { dayItem -> dayItem.day == day }.cells.single { it.attemptId == attemptId }
	}

	// The size the grid gives the cell: a lane wide, as tall as its blocks.
	private fun Modifier.cellSizeOf(cell: ScheduleCellItem): Modifier {
		val height = ScheduleGridDefaults.BlockRowHeight * cell.blockSpan +
			ScheduleGridDefaults.BlockRowGap * (cell.blockSpan - 1)

		return width(ScheduleGridDefaults.LaneWidth).height(height)
	}

	private fun cellOf(attemptId: String, day: ScheduleDay, now: ScheduleNow? = null): ScheduleCellItem {
		return clashingWeek(now = now).grid.days
			.first { dayItem -> dayItem.day == day }
			.cells
			.single { cell -> cell.attemptId == attemptId }
	}
}
