package com.gdavidpb.tuindice.academiccore.domain.model

/**
 * Two meetings that share a block of the same [day], of subjects or sections that are not the same: the
 * student cannot attend both. [first] is the meeting that starts first (see ScheduleClashEngine).
 */
data class ScheduleClash(
	val day: Int,
	val first: ScheduleMeeting,
	val second: ScheduleMeeting
)
