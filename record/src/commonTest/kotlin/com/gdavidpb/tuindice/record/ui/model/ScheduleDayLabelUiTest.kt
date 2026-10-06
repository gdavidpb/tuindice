package com.gdavidpb.tuindice.record.ui.model

import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ScheduleDayLabelUiTest {
	@Test
	fun when_theDayIsAWeekday_then_itsLabelIsItsShortName() = runTuIndiceUiTest {
		var labels = emptyMap<ScheduleDay, String>()

		setTuIndiceTestContent {
			labels = ScheduleDay.entries.filterNot(ScheduleDay::isWeekend).associateWith { day -> day.label() }
		}

		waitForIdle()

		assertEquals(
			expected = mapOf(
				ScheduleDay.Monday to "Lun",
				ScheduleDay.Tuesday to "Mar",
				ScheduleDay.Wednesday to "Mié",
				ScheduleDay.Thursday to "Jue",
				ScheduleDay.Friday to "Vie"
			),
			actual = labels
		)
	}

	@Test
	fun when_theDayIsInTheWeekend_then_itsLabelIsItsShortName() = runTuIndiceUiTest {
		var labels = emptyMap<ScheduleDay, String>()

		setTuIndiceTestContent {
			labels = ScheduleDay.entries.filter(ScheduleDay::isWeekend).associateWith { day -> day.label() }
		}

		waitForIdle()

		assertEquals(
			expected = mapOf(
				ScheduleDay.Saturday to "Sáb",
				ScheduleDay.Sunday to "Dom"
			),
			actual = labels
		)
	}
}
