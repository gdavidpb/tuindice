package com.gdavidpb.tuindice.academiccore.domain.engine

/**
 * The equivalence between the university's class blocks and the clock: block 1 starts at 7:30 and
 * every block lasts 60 minutes, in the device's local time. The product owner confirmed it on
 * 2026-10-11. It lives only here: changing these values moves everything that reads the clock of a
 * class (the "now" line of the week grid, the class in progress, the next class of the day) with it.
 */
object ScheduleBlockClock {
	internal const val FIRST_BLOCK_START_MINUTE = 7 * 60 + 30
	const val BLOCK_MINUTES = 60
	internal const val MINUTES_PER_DAY = 24 * 60

	/**
	 * How many blocks have gone by since block 1 started, as a fraction: 0.0 at 7:30, 1.5 at 9:00.
	 * Negative before the first block. The whole part plus one is the block being taught.
	 */
	fun blocksSinceFirstBlock(minuteOfDay: Int): Float {
		return (minuteOfDay - FIRST_BLOCK_START_MINUTE) / BLOCK_MINUTES.toFloat()
	}

	/**
	 * The block being taught at [minuteOfDay]: 1 from 7:30, 2 from 8:30 and so on, and 0 while block 1
	 * has not started. It keeps counting past the last block the university uses (there is no upper
	 * bound), so the caller decides what is worth showing.
	 */
	fun blockAt(minuteOfDay: Int): Int {
		val minutesSinceFirstBlock = minuteOfDay - FIRST_BLOCK_START_MINUTE

		return if (minutesSinceFirstBlock < 0) 0 else minutesSinceFirstBlock / BLOCK_MINUTES + 1
	}

	/**
	 * Whether [block] starts and ends within one day, that is, whether [blockStartMinute] and
	 * [blockEndMinute] are minutes of that day (0 until [MINUTES_PER_DAY]). Blocks 1 to 16 do; block 17
	 * starts at 23:30 and ends past midnight, and later ones start on the next day. Whoever shows the
	 * hour of a block should check it first.
	 */
	internal fun isWithinDay(block: Int): Boolean {
		return block >= 1 && blockEndMinute(block) <= MINUTES_PER_DAY
	}

	/**
	 * The minute of the day (0 = midnight) at which [block] starts: block 1 starts at minute 450. It is
	 * a plain offset from block 1, so for blocks from 17 on it is [MINUTES_PER_DAY] or more (a minute
	 * of the following day); see [isWithinDay].
	 */
	internal fun blockStartMinute(block: Int): Int {
		return FIRST_BLOCK_START_MINUTE + (block - 1) * BLOCK_MINUTES
	}

	/**
	 * The minute at which [block] ends, which is the minute the next block starts. Like
	 * [blockStartMinute] it can reach [MINUTES_PER_DAY] or more; see [isWithinDay].
	 */
	internal fun blockEndMinute(block: Int): Int {
		return blockStartMinute(block) + BLOCK_MINUTES
	}

	/**
	 * The minutes of the day at which a class from [startBlock] to [endBlock] starts and ends, or null
	 * when either block falls outside the day (see [isWithinDay]): an hour for it would be wrong rather
	 * than late. It is the one place that decides whether a class has an hour to show.
	 */
	fun clockRangeOrNull(startBlock: Int, endBlock: Int): IntRange? {
		if (!isWithinDay(startBlock) || !isWithinDay(endBlock)) return null

		return blockStartMinute(startBlock)..blockEndMinute(endBlock)
	}
}
