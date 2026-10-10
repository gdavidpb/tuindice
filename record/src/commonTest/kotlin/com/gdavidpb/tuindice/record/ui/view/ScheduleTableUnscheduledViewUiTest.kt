package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.ScheduleTableDefaults
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ScheduleTableUnscheduledViewUiTest {
	@Test
	fun when_theSubjectHasNoMeeting_then_itSaysSinHorario_underTheTagOfThatSubject() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleTableUnscheduledView(attemptId = "a3", dayCount = 5)
		}

		assertNodeVisible(RecordUiTags.scheduleTableUnscheduled("a3"))
		assertNodeHidden(RecordUiTags.scheduleTableUnscheduled("a1"))
		onNodeWithText("Sin horario").assertIsDisplayed()
	}

	@Test
	fun when_theWeekShowsFiveDays_then_theMessageTakesTheirFiveColumns() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleTableUnscheduledView(attemptId = "a3", dayCount = 5)
		}

		// Five day cells and the four gaps between them: what the cells it stands for would take.
		onNodeWithTag(RecordUiTags.scheduleTableUnscheduled("a3"))
			.assertWidthIsEqualTo(ScheduleTableDefaults.DayWidth * 5 + ScheduleTableDefaults.CellGap * 4)
	}

	@Test
	fun when_theWeekShowsTheWeekendToo_then_theMessageGrowsWithTheColumns() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleTableUnscheduledView(attemptId = "a3", dayCount = 7)
		}

		onNodeWithTag(RecordUiTags.scheduleTableUnscheduled("a3"))
			.assertWidthIsEqualTo(ScheduleTableDefaults.DayWidth * 7 + ScheduleTableDefaults.CellGap * 6)
		onNodeWithText("Sin horario").assertIsDisplayed()
	}
}
