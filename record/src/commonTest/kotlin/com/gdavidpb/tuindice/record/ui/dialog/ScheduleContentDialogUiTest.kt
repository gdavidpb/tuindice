package com.gdavidpb.tuindice.record.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Schedule
import com.gdavidpb.tuindice.record.presentation.model.ScheduleCellItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDayItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleGridItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleTableItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleTableRowItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

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
		onNodeWithText("Por convenir").assertIsDisplayed()
		onNodeWithText("Choque de horario").assertIsDisplayed()
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
		onNodeWithText("Por convenir: EG1114").assertIsDisplayed()
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

	private fun content(viewMode: ScheduleViewMode): Schedule.State.Content {
		val monday = cell(attemptId = "a1", code = "CI5311", blocks = 1..2, laneCount = 2)
		val wednesday = cell(attemptId = "a1", code = "CI5311", blocks = 1..2)
		val clashing = cell(attemptId = "a2", code = "CI5437", blocks = 2..3, laneCount = 2)
		val days = listOf(
			ScheduleDayItem(day = ScheduleDay.Monday, cells = listOf(monday, clashing)),
			ScheduleDayItem(day = ScheduleDay.Tuesday, cells = emptyList()),
			ScheduleDayItem(day = ScheduleDay.Wednesday, cells = listOf(wednesday)),
			ScheduleDayItem(day = ScheduleDay.Thursday, cells = emptyList()),
			ScheduleDayItem(day = ScheduleDay.Friday, cells = emptyList())
		)

		return Schedule.State.Content(
			termName = "SEP-DIC 2026",
			viewMode = viewMode,
			schedule = ScheduleItem(
				grid = ScheduleGridItem(blockCount = 3, days = days, unscheduledCodes = listOf("EG1114")),
				table = ScheduleTableItem(
					days = days.map { it.day },
					rows = listOf(
						ScheduleTableRowItem(
							attemptId = "a1",
							subjectCode = "CI5311",
							section = 1,
							classroom = "MYS-116",
							errorText = null,
							meetings = mapOf(
								ScheduleDay.Monday to listOf(monday),
								ScheduleDay.Wednesday to listOf(wednesday)
							)
						),
						ScheduleTableRowItem(
							attemptId = "a2",
							subjectCode = "CI5437",
							section = 2,
							classroom = null,
							errorText = "Choque de horario",
							meetings = mapOf(ScheduleDay.Monday to listOf(clashing))
						),
						ScheduleTableRowItem(
							attemptId = "a3",
							subjectCode = "EG1114",
							section = null,
							classroom = null,
							errorText = null,
							meetings = emptyMap()
						)
					)
				)
			)
		)
	}

	private fun cell(
		attemptId: String,
		code: String,
		blocks: IntRange,
		laneCount: Int = 1
	) = ScheduleCellItem(
		attemptId = attemptId,
		codeText = code,
		classroomText = null,
		startBlock = blocks.first,
		endBlock = blocks.last,
		lane = 0,
		laneCount = laneCount
	)
}
