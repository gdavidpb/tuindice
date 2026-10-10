package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Schedule
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.testing.clashingWeek
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ScheduleViewsViewUiTest {
	@Test
	fun when_theChosenViewIsTable_then_onlyTheTableIsComposed_withTheSubjectsOfTheState() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleViewsView(state = content(ScheduleViewMode.Table))
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
			ScheduleViewsView(state = content(ScheduleViewMode.Week))
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
			ScheduleViewsView(state = content(viewMode))
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

	private fun content(viewMode: ScheduleViewMode) = Schedule.State.Content(
		termName = "SEP-DIC 2026",
		viewMode = viewMode,
		schedule = clashingWeek()
	)
}
