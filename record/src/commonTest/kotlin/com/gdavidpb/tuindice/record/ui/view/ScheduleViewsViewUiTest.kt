package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Schedule
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.presentation.model.ScheduleItem
import com.gdavidpb.tuindice.record.testing.ScheduleTestDate
import com.gdavidpb.tuindice.record.testing.clashingWeek
import com.gdavidpb.tuindice.record.testing.markedWeek
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ScheduleViewsViewUiTest {
	@Test
	fun when_theChosenViewIsTable_then_onlyTheTableIsComposed_withTheSubjectsOfTheState() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleViewsView(state = content(ScheduleViewMode.Table), onEvaluationClick = {})
		}

		assertNodeVisible(RecordUiTags.ScheduleTable)
		assertNodeVisible(RecordUiTags.scheduleTableRow("a1"))
		assertNodeVisible(RecordUiTags.scheduleTableRow("a2"))
		assertNodeVisible(RecordUiTags.scheduleTableRow("a3"))
		assertNodeHidden(RecordUiTags.ScheduleContainer)
		assertNodeHidden(RecordUiTags.ScheduleGrid)
	}

	@Test
	fun when_theChosenViewIsWeek_then_onlyTheWeekIsComposed_withTheMeetingsOfTheState() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleViewsView(state = content(ScheduleViewMode.Week), onEvaluationClick = {})
		}

		assertNodeVisible(RecordUiTags.ScheduleContainer)
		assertNodeVisible(RecordUiTags.ScheduleGrid)
		assertNodeVisible(RecordUiTags.scheduleCell("a1", ScheduleDay.Monday.code, 1))
		assertNodeVisible(RecordUiTags.scheduleCell("a2", ScheduleDay.Monday.code, 2))
		onNodeWithText("Sin horario: EG1114").assertIsDisplayed()
		assertNodeHidden(RecordUiTags.ScheduleTable)
	}

	@Test
	fun when_theChosenViewChanges_then_theOtherViewTakesItsPlace() = runTuIndiceUiTest {
		var viewMode by mutableStateOf(ScheduleViewMode.Table)

		setTuIndiceTestContent {
			ScheduleViewsView(state = content(viewMode), onEvaluationClick = {})
		}

		assertNodeVisible(RecordUiTags.ScheduleTable)

		viewMode = ScheduleViewMode.Week
		waitForIdle()

		assertNodeVisible(RecordUiTags.ScheduleGrid)
		assertNodeHidden(RecordUiTags.ScheduleTable)

		viewMode = ScheduleViewMode.Table
		waitForIdle()

		assertNodeVisible(RecordUiTags.ScheduleTable)
		assertNodeHidden(RecordUiTags.ScheduleGrid)
	}

	@Test
	fun when_theTableIsChosen_then_aMarkedCellAsksForItsWeek_andTheGridDoesNotMarkAnything() = runTuIndiceUiTest {
		var viewMode by mutableStateOf(ScheduleViewMode.Table)
		var asked: LocalDate? = null

		setTuIndiceTestContent {
			ScheduleViewsView(
				state = content(viewMode, schedule = markedWeek()),
				onEvaluationClick = { weekStart -> asked = weekStart }
			)
		}

		onNodeWithTag(RecordUiTags.scheduleTableEvaluation("a1", ScheduleDay.Wednesday.code), useUnmergedTree = true)
			.performClick()

		assertEquals(ScheduleTestDate, asked)

		viewMode = ScheduleViewMode.Week
		waitForIdle()

		assertNodeVisible(RecordUiTags.ScheduleGrid)
		assertNodeHidden(RecordUiTags.ScheduleEvaluationLegend)
		assertNodeHidden(RecordUiTags.scheduleTableEvaluation("a1", ScheduleDay.Wednesday.code), useUnmergedTree = true)
	}

	private fun content(viewMode: ScheduleViewMode, schedule: ScheduleItem = clashingWeek()) = Schedule.State.Content(
		subtitle = UiText.Raw("SEP-DIC 2026"),
		viewMode = viewMode,
		schedule = schedule
	)
}
