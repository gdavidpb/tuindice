package com.gdavidpb.tuindice.summary.presentation.model

import kotlinx.datetime.LocalDate

/** Where a line of the hub leads: the screen that owns what the line says. */
sealed interface SummaryHubTarget {
	/** The Schedule sheet, over Summary. */
	data object Schedule : SummaryHubTarget

	/** The Record tab (the term, and the enrollment errors). */
	data object Record : SummaryHubTarget

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
}
