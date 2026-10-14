package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable

/**
 * "Esta semana": the class in progress or next, the first row of the week, then the evaluations that have not gone by.
 * [card] says what the rows leave out: an invitation, what is pending, or what the week lacks. The link to the Schedule
 * sheet is there only when the term has a [hasSchedule]: with none, the sheet would open empty.
 */
@Immutable
data class SummaryWeekItem(
	val classRow: SummaryClassRowItem?,
	val evaluations: List<SummaryEvaluationRowItem>,
	val hasSchedule: Boolean,
	val card: SummaryCardItem?
)
