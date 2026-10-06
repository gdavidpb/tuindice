package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ScheduleViewSwitchViewUiTest {
	@Test
	fun when_tableIsTheChosenView_then_itsIconIsSelected_andEachIconIsReadByItsView() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleViewSwitchView(
				selectedMode = ScheduleViewMode.Table,
				onModeSelected = {}
			)
		}

		assertNodeVisible(RecordUiTags.ScheduleViewSwitch)
		// The icons carry no label on screen: the name of the view is all a screen reader has.
		onNodeWithTag(RecordUiTags.ScheduleViewTableTab)
			.assertIsSelected()
			.assertContentDescriptionEquals("Tabla")
		onNodeWithTag(RecordUiTags.ScheduleViewWeekTab)
			.assertIsNotSelected()
			.assertContentDescriptionEquals("Semana")
	}

	@Test
	fun when_weekIsTheChosenView_then_theSelectionSitsOnItsIcon() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleViewSwitchView(
				selectedMode = ScheduleViewMode.Week,
				onModeSelected = {}
			)
		}

		onNodeWithTag(RecordUiTags.ScheduleViewWeekTab).assertIsSelected()
		onNodeWithTag(RecordUiTags.ScheduleViewTableTab).assertIsNotSelected()
	}

	@Test
	fun when_anIconIsTapped_then_itsViewIsReported_andTheSelectionStaysWithTheCaller() = runTuIndiceUiTest {
		val reported = mutableListOf<ScheduleViewMode>()

		setTuIndiceTestContent {
			ScheduleViewSwitchView(
				selectedMode = ScheduleViewMode.Table,
				onModeSelected = { mode -> reported += mode }
			)
		}

		onNodeWithTag(RecordUiTags.ScheduleViewWeekTab).performClick()
		onNodeWithTag(RecordUiTags.ScheduleViewTableTab).performClick()

		assertEquals(listOf(ScheduleViewMode.Week, ScheduleViewMode.Table), reported)
		// The switch keeps no selection of its own: until the caller hands the new view back, the
		// one it was given stays selected.
		onNodeWithTag(RecordUiTags.ScheduleViewTableTab).assertIsSelected()
		onNodeWithTag(RecordUiTags.ScheduleViewWeekTab).assertIsNotSelected()
	}
}
