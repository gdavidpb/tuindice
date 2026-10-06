package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.record.domain.model.ScheduleNow
import com.gdavidpb.tuindice.record.presentation.model.ScheduleCellItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.testing.MondayAtNine
import com.gdavidpb.tuindice.record.testing.clashingWeek
import com.gdavidpb.tuindice.record.testing.scheduleAttempt
import com.gdavidpb.tuindice.record.testing.scheduleEntry
import com.gdavidpb.tuindice.record.testing.scheduleItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

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
			ScheduleCellView(day = ScheduleDay.Tuesday, cell = cell)
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
			ScheduleCellView(day = ScheduleDay.Wednesday, cell = cell)
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
			ScheduleCellView(day = ScheduleDay.Monday, cell = cell)
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
			ScheduleCellView(day = ScheduleDay.Monday, cell = cell)
		}

		onNodeWithTag(RecordUiTags.scheduleCell("a1", ScheduleDay.Monday.code, 1))
			.assertTextEquals("CI5311", "MYS-116")
			.assertContentDescriptionEquals("CI5311, lunes, bloques 1 a 2, aula MYS-116. Choque de horario. En curso")
	}

	private fun cellOf(attemptId: String, day: ScheduleDay, now: ScheduleNow? = null): ScheduleCellItem {
		return clashingWeek(now = now).grid.days
			.first { dayItem -> dayItem.day == day }
			.cells
			.single { cell -> cell.attemptId == attemptId }
	}
}
