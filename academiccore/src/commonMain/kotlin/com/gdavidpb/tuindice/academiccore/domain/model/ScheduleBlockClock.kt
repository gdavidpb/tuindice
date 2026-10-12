package com.gdavidpb.tuindice.academiccore.domain.model

/**
 * The equivalence between the university's class blocks and the clock: block 1 starts at 7:30 and
 * every block lasts 60 minutes, in the device's local time. The product owner confirmed it on
 * 2026-10-11. It lives only here: changing these values moves everything that reads the clock of a
 * class (the "now" line of the week grid, the class in progress, the next class of the day) with it.
 */
object ScheduleBlockClock {
	const val FIRST_BLOCK_START_MINUTE = 7 * 60 + 30
	const val BLOCK_MINUTES = 60

	/**
	 * How many blocks have gone by since block 1 started, as a fraction: 0.0 at 7:30, 1.5 at 9:00.
	 * Negative before the first block. The whole part plus one is the block being taught.
	 */
	fun blocksSinceFirstBlock(minuteOfDay: Int): Float {
		return (minuteOfDay - FIRST_BLOCK_START_MINUTE) / BLOCK_MINUTES.toFloat()
	}

	/** The minute of the day (0 = midnight) at which [block] starts: block 1 starts at minute 450. */
	fun blockStartMinute(block: Int): Int {
		return FIRST_BLOCK_START_MINUTE + (block - 1) * BLOCK_MINUTES
	}

	/** The minute of the day at which [block] ends, which is the minute the next block starts. */
	fun blockEndMinute(block: Int): Int {
		return blockStartMinute(block) + BLOCK_MINUTES
	}
}
