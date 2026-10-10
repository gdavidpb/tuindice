package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import com.gdavidpb.tuindice.record.domain.model.ScheduleNow
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDayItem
import com.gdavidpb.tuindice.record.testing.MondayAtNine
import com.gdavidpb.tuindice.record.testing.assertDpEquals
import com.gdavidpb.tuindice.record.testing.clashingWeek
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.ScheduleGridDefaults
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ScheduleDayColumnUiTest {
	@Test
	fun when_aMeetingStartsOnALaterBlock_then_itsCellSitsOnThatBlock_andSpansItsBlocks() = runTuIndiceUiTest {
		setTuIndiceTestContent { DayColumn(day(ScheduleDay.Monday)) }

		val column = onNodeWithTag(ColumnTag).getUnclippedBoundsInRoot()
		val first = boundsOfCell(attemptId = "a1", startBlock = 1)
		val second = boundsOfCell(attemptId = "a2", startBlock = 2)

		// CI5311 holds blocks 1-2 from the top; CI5437 holds 2-3, so it starts one row down.
		assertDpEquals(expected = column.top, actual = first.top, what = "top of the cell on block 1")
		assertDpEquals(expected = column.top + RowStep, actual = second.top, what = "top of the cell on block 2")
		assertDpEquals(expected = TwoBlocksHeight, actual = first.bottom - first.top, what = "height of two blocks")
		assertDpEquals(expected = TwoBlocksHeight, actual = second.bottom - second.top, what = "height of two blocks")
	}

	@Test
	fun when_twoMeetingsOverlap_then_eachTakesTheLaneTheLayoutGaveIt() = runTuIndiceUiTest {
		setTuIndiceTestContent { DayColumn(day(ScheduleDay.Monday)) }

		val first = boundsOfCell(attemptId = "a1", startBlock = 1)
		val second = boundsOfCell(attemptId = "a2", startBlock = 2)

		assertDpEquals(
			expected = ScheduleGridDefaults.LaneWidth,
			actual = second.left - first.left,
			what = "from the first lane to the second"
		)
	}

	@Test
	fun when_theDayCarriesNoPosition_then_noNowLineIsDrawnOverIt() = runTuIndiceUiTest {
		// Today is Monday: Wednesday has a meeting to draw but no line.
		setTuIndiceTestContent { DayColumn(day(ScheduleDay.Wednesday, now = MondayAtNine)) }

		assertNodeVisible(RecordUiTags.scheduleCell("a1", ScheduleDay.Wednesday.code, 3))
		assertNodeHidden(RecordUiTags.ScheduleNowLine)
	}

	@Test
	fun when_theDayCarriesThePosition_then_theNowLineCrossesItAtThatHeight() = runTuIndiceUiTest {
		setTuIndiceTestContent { DayColumn(day(ScheduleDay.Monday, now = MondayAtNine)) }

		assertNodeVisible(RecordUiTags.ScheduleNowLine)

		val column = onNodeWithTag(ColumnTag).getUnclippedBoundsInRoot()
		val line = onNodeWithTag(RecordUiTags.ScheduleNowLine).getUnclippedBoundsInRoot()

		// 9:00 is halfway through block 2: one whole row down, then half a block.
		assertDpEquals(
			expected = column.top + RowStep + ScheduleGridDefaults.BlockRowHeight / 2,
			actual = (line.top + line.bottom) / 2,
			what = "height of the now line"
		)
		assertDpEquals(expected = column.left, actual = line.left, what = "where the line starts")
		assertDpEquals(expected = column.right, actual = line.right, what = "where the line ends")
	}

	@Composable
	private fun DayColumn(dayItem: ScheduleDayItem) {
		ScheduleDayColumn(
			modifier = Modifier
				.testTag(ColumnTag)
				.width(ScheduleGridDefaults.LaneWidth * dayItem.laneCount)
				.height(GridHeight),
			dayItem = dayItem
		)
	}

	private fun ComposeUiTest.boundsOfCell(attemptId: String, startBlock: Int): DpRect {
		return onNodeWithTag(RecordUiTags.scheduleCell(attemptId, ScheduleDay.Monday.code, startBlock))
			.getUnclippedBoundsInRoot()
	}

	private fun day(day: ScheduleDay, now: ScheduleNow? = null): ScheduleDayItem {
		return clashingWeek(now = now).grid.days.first { dayItem -> dayItem.day == day }
	}

	private companion object {
		const val ColumnTag = "day_column"

		val RowStep: Dp = ScheduleGridDefaults.BlockRowHeight + ScheduleGridDefaults.BlockRowGap
		val TwoBlocksHeight: Dp = ScheduleGridDefaults.BlockRowHeight * 2 + ScheduleGridDefaults.BlockRowGap

		// The three blocks of the week being drawn, with the two gaps between them.
		val GridHeight: Dp = ScheduleGridDefaults.BlockRowHeight * 3 + ScheduleGridDefaults.BlockRowGap * 2
	}
}
