package com.gdavidpb.tuindice.record.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicScheduleEntry
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.record.domain.model.ScheduleNow
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Schedule
import com.gdavidpb.tuindice.record.presentation.mapper.toScheduleItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalTestApi::class)
class ScheduleContentDialogUiTest {
	@Test
	fun when_viewIsTable_then_rowsShowCodeDetailBlocksAndTheClash() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleContentDialog(
				state = content(ScheduleViewMode.Table),
				onViewModeSelected = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(RecordUiTags.ScheduleTable)
		assertNodeHidden(RecordUiTags.ScheduleGrid)
		onNodeWithText("CI5311").assertIsDisplayed()
		onNodeWithText("Sec. 1 · MYS-116").assertIsDisplayed()
		onNodeWithText("Sec. 2").assertIsDisplayed()
		onNodeWithText("Sin horario").assertIsDisplayed()
		// The clash the app works out hangs under both subjects involved, naming neither; what the
		// university wrote on one of them stays on its card in the record.
		onAllNodesWithText("Choque de horario").assertCountEquals(2)
		assertNodeVisible(RecordUiTags.scheduleTableError("a1"))
		assertNodeVisible(RecordUiTags.scheduleTableError("a2"))
		assertNodeHidden(RecordUiTags.scheduleTableError("a3"))
		onAllNodesWithText("credito", substring = true, ignoreCase = true).assertCountEquals(0)
		// The clash mark lives inside the merged row, so it is read from the unmerged tree.
		assertNodeVisible(RecordUiTags.scheduleTableClash("a1", ScheduleDay.Monday.code), useUnmergedTree = true)
		onAllNodesWithTag(RecordUiTags.scheduleTableClash("a1", ScheduleDay.Wednesday.code), useUnmergedTree = true)
			.assertCountEquals(0)
		onNode(hasContentDescription("CI5311, sección 1, aula MYS-116, lunes bloques 1 a 2, miércoles bloques 1 a 2"))
			.assertIsDisplayed()
	}

	@Test
	fun when_viewIsWeek_then_theGridAndTheUnscheduledLineAreShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleContentDialog(
				state = content(ScheduleViewMode.Week),
				onViewModeSelected = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(RecordUiTags.ScheduleGrid)
		assertNodeHidden(RecordUiTags.ScheduleTable)
		onNodeWithText("Sin horario: EG1114").assertIsDisplayed()
		// A cell that overlaps another says the clash aloud; the same subject on a clear day does not.
		onNode(hasContentDescription("CI5311, lunes, bloques 1 a 2, aula MYS-116. Choque de horario"))
			.assertIsDisplayed()
		onNode(hasContentDescription("CI5437, lunes, bloques 2 a 3. Choque de horario")).assertIsDisplayed()
		onNode(hasContentDescription("CI5311, miércoles, bloques 1 a 2, aula MYS-116")).assertIsDisplayed()
	}

	@Test
	fun when_viewIsTableAndAClassIsBeingTaught_then_todayAndTheClassInProgressAreMarked() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleContentDialog(
				// Monday 9:00, halfway through block 2: both Monday meetings hold it, and they clash.
				state = content(ScheduleViewMode.Table, now = ScheduleNow(dayOfWeek = 2, minuteOfDay = 9 * 60)),
				onViewModeSelected = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(RecordUiTags.ScheduleTodayHeader)
		onNode(hasContentDescription("lunes, hoy")).assertIsDisplayed()
		assertNodeVisible(RecordUiTags.scheduleTableInProgress("a1", ScheduleDay.Monday.code), useUnmergedTree = true)
		// In progress and in clash at once: each mark keeps its own tag.
		assertNodeVisible(RecordUiTags.scheduleTableClash("a1", ScheduleDay.Monday.code), useUnmergedTree = true)
		assertNodeHidden(RecordUiTags.scheduleTableInProgress("a1", ScheduleDay.Wednesday.code), useUnmergedTree = true)
		assertNodeHidden(RecordUiTags.ScheduleNowLine)
		onNode(
			hasContentDescription(
				"CI5311, sección 1, aula MYS-116, lunes bloques 1 a 2, en curso, miércoles bloques 1 a 2"
			)
		).assertIsDisplayed()
	}

	@Test
	fun when_viewIsWeekAndTheHourFallsInTheGrid_then_todayAndTheNowLineAreDrawn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleContentDialog(
				state = content(ScheduleViewMode.Week, now = ScheduleNow(dayOfWeek = 2, minuteOfDay = 9 * 60)),
				onViewModeSelected = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(RecordUiTags.ScheduleTodayHeader)
		assertNodeVisible(RecordUiTags.ScheduleNowLine)
		onNode(hasContentDescription("Ahora")).assertIsDisplayed()
		onNode(hasContentDescription("CI5311, lunes, bloques 1 a 2, aula MYS-116. Choque de horario. En curso"))
			.assertIsDisplayed()
	}

	@Test
	fun when_todayIsNotInTheWeek_then_nothingIsHighlighted() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleContentDialog(
				// A Sunday with nothing on Sunday, at an hour that would otherwise fall inside the grid.
				state = content(ScheduleViewMode.Week, now = ScheduleNow(dayOfWeek = 1, minuteOfDay = 9 * 60)),
				onViewModeSelected = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(RecordUiTags.ScheduleGrid)
		assertNodeHidden(RecordUiTags.ScheduleTodayHeader)
		assertNodeHidden(RecordUiTags.ScheduleNowLine)
	}

	@Test
	fun when_aViewIsTapped_then_itIsReported() = runTuIndiceUiTest {
		var selected: ScheduleViewMode? = null

		setTuIndiceTestContent {
			ScheduleContentDialog(
				state = content(ScheduleViewMode.Table),
				onViewModeSelected = { selected = it },
				onDismissRequest = {}
			)
		}

		onNodeWithTag(RecordUiTags.ScheduleViewWeekTab).performClick()

		assertEquals(ScheduleViewMode.Week, selected)
	}

	@Test
	fun when_nothingIsScheduled_then_theEmptyCopyIsShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleContentDialog(
				state = Schedule.State.Empty,
				onViewModeSelected = {},
				onDismissRequest = {}
			)
		}

		// The sheet keeps its title; the body says why there is nothing under it.
		onNodeWithText("Horario").assertIsDisplayed()
		onNodeWithText("Tu trimestre actual todavía no tiene horario.").assertIsDisplayed()
	}

	@Test
	fun when_loading_then_theSpinnerIsShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleContentDialog(
				state = Schedule.State.Loading,
				onViewModeSelected = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(RecordUiTags.ScheduleLoadingIndicator)
	}

	// Built by the mapper, so the texts the sheet shows and says are the ones the app resolves.
	private fun content(viewMode: ScheduleViewMode, now: ScheduleNow? = null): Schedule.State.Content {
		val attempts = listOf(
			attempt(
				id = "a1",
				code = "CI5311",
				section = 1,
				schedule = listOf(
					entry(day = ScheduleDay.Monday, blocks = 1..2, classroom = "MYS-116"),
					entry(day = ScheduleDay.Wednesday, blocks = 1..2, classroom = "MYS-116")
				)
			),
			attempt(
				id = "a2",
				code = "CI5437",
				section = 2,
				schedule = listOf(entry(day = ScheduleDay.Monday, blocks = 2..3)),
				errors = listOf("CHOQUE DE HORARIO CON CI5311", "EXCEDE EL LIMITE DE CREDITOS")
			),
			attempt(id = "a3", code = "EG1114")
		)

		return Schedule.State.Content(
			termName = "SEP-DIC 2026",
			viewMode = viewMode,
			schedule = assertNotNull(attempts.toScheduleItem(now = now))
		)
	}

	private fun entry(day: ScheduleDay, blocks: IntRange, classroom: String = "") = AcademicScheduleEntry(
		dayOfWeek = day.code,
		startBlock = blocks.first,
		endBlock = blocks.last,
		classroom = classroom
	)

	private fun attempt(
		id: String,
		code: String,
		section: Int? = null,
		schedule: List<AcademicScheduleEntry>? = null,
		errors: List<String>? = null
	) = AttemptProjection(
		id = id,
		subjectCode = code,
		subjectName = code,
		credits = 3,
		gradingMode = AttemptGradingMode.NUMERIC,
		score = AttemptScore.empty(),
		outcome = AttemptOutcome.PENDING,
		badge = AttemptBadge.NONE,
		section = section,
		schedule = schedule,
		enrollmentErrors = errors
	)
}
