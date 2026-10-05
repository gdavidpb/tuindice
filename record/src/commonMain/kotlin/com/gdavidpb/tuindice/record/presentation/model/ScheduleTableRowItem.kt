package com.gdavidpb.tuindice.record.presentation.model

// A subject and its meetings by day. The cells are the grid's own, so a meeting that shares its
// day with another subject's (isClash) is the same clash in both views.
data class ScheduleTableRowItem(
	val attemptId: String,
	val subjectCode: String,
	val section: Int?,
	// Only when every meeting that names a room agrees on it.
	val classroom: String?,
	// What the university flagged on the enrollment of the subject, as the chip on its row says it.
	val errorText: String?,
	val meetings: Map<ScheduleDay, List<ScheduleCellItem>>
) {
	// Nothing placed on any day: the schedule is still to be agreed.
	val isUnscheduled: Boolean
		get() = meetings.isEmpty()
}
