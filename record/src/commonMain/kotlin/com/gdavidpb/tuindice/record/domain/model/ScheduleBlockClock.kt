package com.gdavidpb.tuindice.record.domain.model

/**
 * The assumed equivalence between the university's class blocks and the clock: block 1 starts at
 * 7:30 and every block lasts 60 minutes, in the device's local time. The university has not
 * confirmed it (it is pending in the backend's backlog). It lives only here: changing these values
 * moves the "now" line of the week grid and the class in progress of the table with them.
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
}
