package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Schedule
import com.gdavidpb.tuindice.record.presentation.model.ScheduleGridItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleTableItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.schedule_subtitle_week
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ScheduleHeaderViewUiTest {
	@Test
	fun when_thereIsNoScheduleYet_then_onlyTheTitleIsShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleHeaderView(
				content = null,
				onViewModeSelected = {}
			)
		}

		onNodeWithTag(RecordUiTags.ScheduleTitle).assertIsDisplayed()
		assertNodeHidden(RecordUiTags.ScheduleViewSwitch)
	}

	@Test
	fun when_theDateIsInAWeekOfTheTerm_then_theSubtitleSaysWhichWeek() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleHeaderView(
				content = Schedule.State.Content(
					subtitle = UiText.Resource(Res.string.schedule_subtitle_week, listOf("Sep - Dic 2026", 3)),
					schedule = ScheduleItem(
						grid = ScheduleGridItem(blockCount = 1, days = emptyList(), unscheduledText = null),
						table = ScheduleTableItem(days = emptyList(), rows = emptyList())
					),
					viewMode = ScheduleViewMode.Table
				),
				onViewModeSelected = {}
			)
		}

		onNodeWithText("Sep - Dic 2026 · Semana 3").assertIsDisplayed()
	}

	@Test
	fun when_thereIsASchedule_then_theTermSitsUnderTheTitle_andTheSwitchReportsTheViewTapped() = runTuIndiceUiTest {
		var selected: ScheduleViewMode? = null

		setTuIndiceTestContent {
			ScheduleHeaderView(
				content = Schedule.State.Content(
					subtitle = UiText.Raw("SEP-DIC 2026"),
					schedule = ScheduleItem(
						grid = ScheduleGridItem(blockCount = 1, days = emptyList(), unscheduledText = null),
						table = ScheduleTableItem(days = emptyList(), rows = emptyList())
					),
					viewMode = ScheduleViewMode.Table
				),
				onViewModeSelected = { selected = it }
			)
		}

		onNodeWithText("Horario").assertIsDisplayed()
		onNodeWithText("SEP-DIC 2026").assertIsDisplayed()
		assertNodeVisible(RecordUiTags.ScheduleViewSwitch)

		onNodeWithTag(RecordUiTags.ScheduleViewWeekTab).performClick()

		assertEquals(ScheduleViewMode.Week, selected)
	}
}
