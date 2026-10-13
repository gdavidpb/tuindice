package com.gdavidpb.tuindice.summary.domain.model

/**
 * The calendar week (Monday to Sunday) that holds today: the class in progress or next one, if the
 * term has a schedule, and the evaluations that fall from today to the end of the week.
 * [hasSchedule] says whether the term has any valid class meeting at all (of a subject graded in
 * numbers or not), which is what makes the Schedule sheet worth opening: the week can exist only
 * for an evaluation.
 */
data class SummaryWeek(
	val classMoment: SummaryClassMoment?,
	val evaluations: List<SummaryWeekEvaluation>,
	val hasSchedule: Boolean
)
