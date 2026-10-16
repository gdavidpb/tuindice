package com.gdavidpb.tuindice.record.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import kotlinx.datetime.LocalDate

/**
 * Which meetings of the schedule have an evaluation in the calendar week being lived. [weekStart]
 * is the Monday that opens that week (the week the evaluations of [evaluationIdByMeeting] are in, and
 * the one the Evaluations tab is asked to show). [evaluationIdByMeeting] maps each meeting those
 * evaluations inherit as their place to the id of the one that marks it: the earliest of them by day
 * within the week and, on a tie of day, the smallest id, so the same evaluations always name the same
 * one. One entry per meeting however many evaluations fall on it.
 */
data class ScheduleEvaluationMarks(
	val weekStart: LocalDate,
	val evaluationIdByMeeting: Map<ScheduleMeeting, String>
)
