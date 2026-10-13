package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable

/**
 * "Esta semana": the class in progress or next, then the evaluations that have not gone by. The link
 * to the Schedule sheet is there only when the term has a [hasSchedule]: with none, the sheet would
 * open empty.
 */
@Immutable
data class SummaryWeekItem(
	val classRow: SummaryClassRowItem?,
	val evaluations: List<SummaryEvaluationRowItem>,
	val hasSchedule: Boolean
)
