package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.testing.MARKED_WEDNESDAY_EVALUATION_ID
import com.gdavidpb.tuindice.record.testing.MondayAtNine
import com.gdavidpb.tuindice.record.testing.ScheduleTestDate
import com.gdavidpb.tuindice.record.testing.assertDpEquals
import com.gdavidpb.tuindice.record.testing.clashingWeek
import com.gdavidpb.tuindice.record.testing.markedWeek
import com.gdavidpb.tuindice.record.testing.scheduleNow
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.ScheduleGridDefaults
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalTestApi::class)
class ScheduleGridViewUiTest {
	@Test
	fun when_theWeekIsLaidOut_then_itsDaysBlocksAndMeetingsAreDrawn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleGridView(grid = clashingWeek().grid, onEvaluationClick = { _, _ -> })
		}

		assertNodeVisible(RecordUiTags.ScheduleGrid)
		onNodeWithText("Lun").assertIsDisplayed()
		onNodeWithText("Mié").assertIsDisplayed()
		// The last days start past the edge of a phone: they are there, a swipe away.
		onNodeWithText("Vie").assertExists()
		onAllNodesWithText("Sáb").assertCountEquals(0)
		// The grid is as tall as the last block anything meets on.
		onNodeWithText("1").assertIsDisplayed()
		onNodeWithText("3").assertIsDisplayed()
		onAllNodesWithText("4").assertCountEquals(0)
		assertNodeVisible(RecordUiTags.scheduleCell("a1", ScheduleDay.Monday.code, 1))
		assertNodeVisible(RecordUiTags.scheduleCell("a2", ScheduleDay.Monday.code, 2))
		assertNodeVisible(RecordUiTags.scheduleCell("a1", ScheduleDay.Wednesday.code, 3))
		// No moment was given: no day is today and there is no line to draw.
		assertNodeHidden(RecordUiTags.ScheduleTodayHeader)
		assertNodeHidden(RecordUiTags.ScheduleNowLine)
	}

	@Test
	fun when_twoSubjectsClash_then_eachKeepsAWholeCell_sideBySide() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleGridView(grid = clashingWeek().grid, onEvaluationClick = { _, _ -> })
		}

		val first = onNodeWithTag(RecordUiTags.scheduleCell("a1", ScheduleDay.Monday.code, 1))
			.getUnclippedBoundsInRoot()
		val second = onNodeWithTag(RecordUiTags.scheduleCell("a2", ScheduleDay.Monday.code, 2))
			.getUnclippedBoundsInRoot()

		assertDpEquals(
			expected = ScheduleGridDefaults.LaneWidth,
			actual = second.left - first.left,
			what = "distance between the two cells that clash"
		)
		assertDpEquals(expected = first.right - first.left, actual = second.right - second.left, what = "cell width")
	}

	@Test
	fun when_aMeetingHasAnEvaluation_then_itsCellAsksForThatWeek_andTheOthersDoNot() = runTuIndiceUiTest {
		var asked: LocalDate? = null
		var askedEvaluationId: String? = null

		setTuIndiceTestContent {
			ScheduleGridView(
				grid = markedWeek().grid,
				onEvaluationClick = { weekStart, evaluationId ->
					asked = weekStart
					askedEvaluationId = evaluationId
				}
			)
		}

		assertNodeVisible(RecordUiTags.scheduleCellEvaluation("a1", ScheduleDay.Wednesday.code, 3), useUnmergedTree = true)
		assertNodeHidden(RecordUiTags.scheduleCellEvaluation("a1", ScheduleDay.Monday.code, 1), useUnmergedTree = true)

		onNodeWithTag(RecordUiTags.scheduleCell("a1", ScheduleDay.Monday.code, 1)).performClick()

		assertNull(asked)

		onNodeWithTag(RecordUiTags.scheduleCell("a1", ScheduleDay.Wednesday.code, 3)).performClick()

		assertEquals(ScheduleTestDate, asked)
		assertEquals(MARKED_WEDNESDAY_EVALUATION_ID, askedEvaluationId)
	}

	@Test
	fun when_theHourFallsInsideTheGrid_then_todayIsMarked_andTheNowLineCrossesIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleGridView(grid = clashingWeek(now = MondayAtNine).grid, onEvaluationClick = { _, _ -> })
		}

		assertNodeVisible(RecordUiTags.ScheduleTodayHeader)
		onNodeWithTag(RecordUiTags.ScheduleTodayHeader).assertContentDescriptionEquals("lunes, hoy")
		assertNodeVisible(RecordUiTags.ScheduleNowLine)
		onNodeWithTag(RecordUiTags.ScheduleNowLine).assertContentDescriptionEquals("Ahora")
	}

	@Test
	fun when_theHourIsPastTheLastBlock_then_todayIsStillMarked_butNoLineIsDrawn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			// Monday at 20:00: the grid ends at block 3, hours before.
			ScheduleGridView(
				grid = clashingWeek(now = scheduleNow(dayOfWeek = 2, minuteOfDay = 20 * 60)).grid,
				onEvaluationClick = { _, _ -> }
			)
		}

		assertNodeVisible(RecordUiTags.ScheduleTodayHeader)
		assertNodeHidden(RecordUiTags.ScheduleNowLine)
	}
}
