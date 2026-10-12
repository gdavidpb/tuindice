package com.gdavidpb.tuindice.academiccore.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduleBlockClockTest {
	@Test
	fun blockOneStartsAtSevenThirtyAndEveryBlockLastsAnHour() {
		assertEquals(450, ScheduleBlockClock.blockStartMinute(1))
		assertEquals(510, ScheduleBlockClock.blockEndMinute(1))
		assertEquals(510, ScheduleBlockClock.blockStartMinute(2))
		assertEquals(7 * 60 + 30 + 10 * 60, ScheduleBlockClock.blockStartMinute(11))
	}

	@Test
	fun aBlockEndsWhereTheNextOneStarts() {
		(1..23).forEach { block ->
			assertEquals(
				ScheduleBlockClock.blockStartMinute(block + 1),
				ScheduleBlockClock.blockEndMinute(block),
				"block $block"
			)
		}
	}

	@Test
	fun blocksSinceFirstBlock_isFractionalAndNegativeBeforeTheFirstBlock() {
		assertEquals(0f, ScheduleBlockClock.blocksSinceFirstBlock(7 * 60 + 30))
		assertEquals(1.5f, ScheduleBlockClock.blocksSinceFirstBlock(9 * 60))
		assertEquals(-0.5f, ScheduleBlockClock.blocksSinceFirstBlock(7 * 60))
	}
}
