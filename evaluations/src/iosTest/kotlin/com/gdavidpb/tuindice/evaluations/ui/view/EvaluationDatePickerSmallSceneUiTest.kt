package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import com.gdavidpb.tuindice.evaluations.presentation.mapper.getEvaluationDateCalendarItem
import com.gdavidpb.tuindice.evaluations.presentation.utils.toEvaluationEpochMillis
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// The dialog's own window is as big as the scene, so a short phone is only reproduced by sizing the
// scene (the common harness fixes the content box, not the window): iOS only, like every Compose UI
// test of the repo.
@OptIn(ExperimentalTestApi::class)
class EvaluationDatePickerSmallSceneUiTest {
	@Test
	fun when_theSceneIsShortAndTheMonthHasSixRows_then_theChosenDayBoxCanBeScrolledWhole() {
		// November 2026 starts on a Sunday: six rows. 2026-11-16 is a Monday, a class day.
		val monday = LocalDate(2026, 11, 16)

		runSkikoComposeUiTest(size = Size(360f, 568f), density = Density(1f)) {
			setTuIndiceTestContent {
				EvaluationDatePicker(
					selectedScheduleMode = EvaluationScheduleMode.DATED,
					selectedDate = monday.toEvaluationEpochMillis(),
					onDateChange = {},
					calendar = getEvaluationDateCalendarItem(
						selectableRange = LocalDate(2026, 11, 1)..LocalDate(2026, 11, 30),
						subjectCode = "CI5311",
						meetings = listOf(
							ScheduleMeeting(
								attemptId = "a1",
								subjectCode = "CI5311",
								section = 1,
								dayOfWeek = 2,
								startBlock = 1,
								endBlock = 2,
								classroom = "MYS-116"
							)
						)
					)
				)
			}

			onNodeWithTag(EvaluationsUiTags.EvaluationDateSelectButton).performClick()
			waitUntil(timeoutMillis = 5_000L) {
				onAllNodesWithText("En clase · 1-2 · MYS-116").fetchSemanticsNodes().isNotEmpty()
			}

			val summary = onNodeWithTag(EvaluationsUiTags.EvaluationDateDialogSelection)

			summary.assertExists()
			// It is reachable: the body scrolls it into view instead of squeezing it.
			summary.performScrollTo()
			waitForIdle()

			val whole = summary.getUnclippedBoundsInRoot().height
			val visible = summary.getBoundsInRoot().height

			assertTrue(whole >= 80.dp, "the box keeps its two lines: $whole")
			assertEquals(whole, visible, "the box is fully in view once scrolled to")
		}
	}
}
