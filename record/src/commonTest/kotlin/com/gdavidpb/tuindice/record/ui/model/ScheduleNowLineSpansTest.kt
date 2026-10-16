package com.gdavidpb.tuindice.record.ui.model

import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDayItem
import com.gdavidpb.tuindice.record.testing.clashingWeek
import kotlin.test.Test
import kotlin.test.assertEquals

// Monday of the clashing week: CI5311 holds blocks 1-2 on the first lane and CI5437 holds 2-3 on the
// second, so the line runs over one, the other, both or none depending on the block of the hour.
class ScheduleNowLineSpansTest {
	private val monday: ScheduleDayItem =
		clashingWeek().grid.days.first { dayItem -> dayItem.day == ScheduleDay.Monday }

	private val laneWidth = ScheduleGridDefaults.LaneWidth
	private val firstLane = TuIndiceSpacing.Hairline..(laneWidth - TuIndiceSpacing.Hairline)
	private val secondLane = (laneWidth + TuIndiceSpacing.Hairline)..(laneWidth * 2 - TuIndiceSpacing.Hairline)

	@Test
	fun withoutAPosition_thereIsNoStretchOverAMeeting() {
		assertEquals(emptyList(), monday.copy(nowBlockOffset = null).nowLineCellSpans())
	}

	@Test
	fun inABlockOnlyTheFirstMeetingHolds_theLineRunsOverItsLane() {
		assertEquals(listOf(firstLane), monday.copy(nowBlockOffset = 0.5f).nowLineCellSpans())
	}

	@Test
	fun inTheBlockBothMeetingsHold_theLineRunsOverBothLanes() {
		assertEquals(listOf(firstLane, secondLane), monday.copy(nowBlockOffset = 1.5f).nowLineCellSpans())
	}

	@Test
	fun atTheVeryStartOfABlock_theLineBelongsToThatBlock() {
		// 2.0 is the top of block 3, which only the second meeting holds.
		assertEquals(listOf(secondLane), monday.copy(nowBlockOffset = 2f).nowLineCellSpans())
	}

	@Test
	fun inABlockNoMeetingHolds_thereIsNoStretchOverAMeeting() {
		assertEquals(emptyList(), monday.copy(nowBlockOffset = 3.25f).nowLineCellSpans())
	}

	@Test
	fun aMeetingIsAsWideAsItsLaneLessTheHairlineOnEachSide() {
		val span = monday.copy(nowBlockOffset = 0.5f).nowLineCellSpans().single()

		assertEquals(72.dp - TuIndiceSpacing.Hairline * 2, span.endInclusive - span.start)
	}
}
