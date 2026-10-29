package com.gdavidpb.tuindice.summary.domain.model

/**
 * The line of the summary that offers a term report the student has not opened: the [insightId] it opens, the [termKey]
 * and [periodLabel] of its term, and the [subtitle] the report gives of itself.
 */
data class SummaryTermReportEntry(
	val insightId: String,
	val termKey: String,
	val periodLabel: String,
	val subtitle: String
)
