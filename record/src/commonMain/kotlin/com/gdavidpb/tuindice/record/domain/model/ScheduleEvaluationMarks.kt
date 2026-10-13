package com.gdavidpb.tuindice.record.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import kotlinx.datetime.LocalDate

/**
 * Which meetings of the schedule have an evaluation in the calendar week being lived. [weekStart]
 * is the Monday that opens that week (the week the evaluations of [meetings] are in, and the one the
 * Evaluations tab is asked to show), and [meetings] are the meetings those evaluations inherit as
 * their place: one entry per meeting however many evaluations fall on it.
 */
data class ScheduleEvaluationMarks(
	val weekStart: LocalDate,
	val meetings: Set<ScheduleMeeting>
)
