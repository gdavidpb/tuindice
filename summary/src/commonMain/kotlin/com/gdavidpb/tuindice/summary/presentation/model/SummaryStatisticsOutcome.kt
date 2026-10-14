package com.gdavidpb.tuindice.summary.presentation.model

/**
 * The outcome a segment of a stacked bar stands for. The screen gives each one its color: the approved and the failed
 * with the status colors, the withdrawn and the subjects without result with the neutral one.
 */
enum class SummaryStatisticsOutcome {
	APPROVED,
	FAILED,
	WITHDRAWN,
	NO_RESULT
}
