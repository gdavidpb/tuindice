package com.gdavidpb.tuindice.summary.presentation.model

import kotlinx.datetime.LocalDate

/** Where a line of the hub leads: the screen that owns what the line says. */
sealed interface SummaryHubTarget {
	/** The Schedule sheet, over Summary. */
	data object Schedule : SummaryHubTarget

	/** The Record tab, on the term it shows. */
	data object Record : SummaryHubTarget

	/** The Record tab, on the term of the attempt [attemptId], with that attempt's card brought into view. */
	data class RecordAttempt(
		val attemptId: String
	) : SummaryHubTarget

	/** The Pensum tab (the career). */
	data object Pensum : SummaryHubTarget

	/**
	 * The Evaluations tab, on the week that [weekStart] (a Monday) opens, with [evaluationId] brought
	 * into view inside it.
	 */
	data class EvaluationsWeek(
		val weekStart: LocalDate,
		val evaluationId: String
	) : SummaryHubTarget

	/** The Evaluations tab, at the form that adds an evaluation. */
	data object AddEvaluation : SummaryHubTarget

	/** The Record tab, at the form that plans a term. */
	data object CreateTerm : SummaryHubTarget
}
