package com.gdavidpb.tuindice.academiccore.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

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

	@Test
	fun blockAt_isZeroBeforeTheFirstBlockAndCountsFromThere() {
		assertEquals(0, ScheduleBlockClock.blockAt(0))
		assertEquals(0, ScheduleBlockClock.blockAt(ScheduleBlockClock.FIRST_BLOCK_START_MINUTE - 1))
		assertEquals(1, ScheduleBlockClock.blockAt(ScheduleBlockClock.FIRST_BLOCK_START_MINUTE))
		assertEquals(1, ScheduleBlockClock.blockAt(ScheduleBlockClock.blockEndMinute(1) - 1))
		assertEquals(2, ScheduleBlockClock.blockAt(ScheduleBlockClock.blockStartMinute(2)))
		assertEquals(17, ScheduleBlockClock.blockAt(ScheduleBlockClock.MINUTES_PER_DAY - 1))
	}

	@Test
	fun blockAt_agreesWithTheStartAndEndMinutesOfEveryBlockWithinTheDay() {
		(1..16).forEach { block ->
			assertEquals(block, ScheduleBlockClock.blockAt(ScheduleBlockClock.blockStartMinute(block)), "start of $block")
			assertEquals(block, ScheduleBlockClock.blockAt(ScheduleBlockClock.blockEndMinute(block) - 1), "end of $block")
		}
	}

	@Test
	fun isWithinDay_isTrueUpToBlockSixteenAndFalseFromSeventeenOn() {
		assertFalse(ScheduleBlockClock.isWithinDay(0))
		assertTrue(ScheduleBlockClock.isWithinDay(1))
		assertTrue(ScheduleBlockClock.isWithinDay(16))
		assertEquals(23 * 60 + 30, ScheduleBlockClock.blockEndMinute(16))
		assertFalse(ScheduleBlockClock.isWithinDay(17))
		assertFalse(ScheduleBlockClock.isWithinDay(24))
	}

	@Test
	fun clockRangeOrNull_goesFromTheStartOfTheFirstBlockToTheEndOfTheLast() {
		assertEquals(450..570, ScheduleBlockClock.clockRangeOrNull(startBlock = 1, endBlock = 2))
		assertEquals(570..630, ScheduleBlockClock.clockRangeOrNull(startBlock = 3, endBlock = 3))
	}

	@Test
	fun clockRangeOrNull_isNullWhenEitherBlockFallsOutsideTheDay() {
		assertNull(ScheduleBlockClock.clockRangeOrNull(startBlock = 17, endBlock = 17))
		assertNull(ScheduleBlockClock.clockRangeOrNull(startBlock = 15, endBlock = 17))
		assertNull(ScheduleBlockClock.clockRangeOrNull(startBlock = 0, endBlock = 1))
		assertEquals(
			ScheduleBlockClock.blockStartMinute(15)..ScheduleBlockClock.blockEndMinute(16),
			ScheduleBlockClock.clockRangeOrNull(startBlock = 15, endBlock = 16)
		)
	}
}
