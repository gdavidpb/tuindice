package com.gdavidpb.tuindice.academiccore.domain.model

/**
 * What a schedule has to say about a moment of the week: the class being taught, or the next one.
 */
sealed interface ScheduleMoment {
	/** [meeting] is in progress: its block range contains the block the clock is in. */
	data class InProgress(val meeting: ScheduleMeeting) : ScheduleMoment

	/**
	 * [meeting] is the next one to start, [daysAhead] days from the day asked (0 = later today,
	 * 1 = tomorrow, up to 6).
	 */
	data class Next(val meeting: ScheduleMeeting, val daysAhead: Int) : ScheduleMoment
}
