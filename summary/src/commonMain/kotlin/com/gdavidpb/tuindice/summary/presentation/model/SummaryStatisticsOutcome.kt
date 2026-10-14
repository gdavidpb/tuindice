package com.gdavidpb.tuindice.summary.presentation.model

/**
 * The outcome a segment of a stacked bar stands for: the approved, the failed and the withdrawn, each drawn in its own
 * colour. The subjects without a result are no segment; they are counted in a line of text beside the bar.
 */
enum class SummaryStatisticsOutcome {
	APPROVED,
	FAILED,
	WITHDRAWN
}
