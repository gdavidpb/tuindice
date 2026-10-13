package com.gdavidpb.tuindice.summary.domain.model

/**
 * The calendar week (Monday to Sunday) that holds today: the class in progress or next one, if the
 * term has a schedule, and the evaluations that fall from today to the end of the week.
 */
data class SummaryWeek(
	val classMoment: SummaryClassMoment?,
	val evaluations: List<SummaryWeekEvaluation>
)
