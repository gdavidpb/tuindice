package com.gdavidpb.tuindice.record.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// A subject and its meetings by day, with what the row says already resolved. The cells are the
// grid's own, so a meeting that shares its day with another subject's (isClash) is the same clash
// in both views.
data class ScheduleTableRowItem(
	val attemptId: String,
	val subjectCode: String,
	// "Sec. 1 · MYS-116". The classroom is named only when every meeting that names a room agrees
	// on it, stands alone when the section is unknown, and a subject with neither has no detail.
	val detailText: UiText?,
	// What a screen reader says of the row: "CI5311, sección 1, aula MYS-116, lunes bloques 1 a 2".
	val description: UiText,
	val meetings: Map<ScheduleDay, List<ScheduleCellItem>>
) {
	// Nothing placed on any day: the schedule is still to be agreed.
	val isUnscheduled: Boolean
		get() = meetings.isEmpty()
}
