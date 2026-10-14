package com.gdavidpb.tuindice.summary.domain.model

/**
 * "Esta semana": the calendar week (Monday to Sunday) that holds today, for the term in progress, or, when no term is
 * in progress, only the fact of having terms or not.
 */
sealed interface SummaryWeek {
	/**
	 * A term is in progress. [classMoment] is its class in progress or the next one, and it is null only when the term
	 * has no class at all; [evaluations] are the dated ones from today to the Sunday that closes the week, and
	 * [hasSchedule] says whether the term has any valid class meeting at all.
	 *
	 * [hasSyncedEvaluations] says whether the evaluations were downloaded: until they were, nothing is known about them,
	 * so the week says nothing of them. [hasEvaluations] says whether the term has any evaluation registered at all, and
	 * [nextEvaluation] is the earliest dated one without a grade that falls from tomorrow on, for a week with
	 * nothing else.
	 */
	data class InProgress(
		val classMoment: SummaryClassMoment?,
		val evaluations: List<SummaryWeekEvaluation>,
		val hasSchedule: Boolean,
		val hasSyncedEvaluations: Boolean,
		val hasEvaluations: Boolean,
		val nextEvaluation: SummaryNextEvaluation?
	) : SummaryWeek

	/** No term is in progress. [hasTerms] says whether the record has any term at all: a new student has none. */
	data class Idle(
		val hasTerms: Boolean
	) : SummaryWeek
}
