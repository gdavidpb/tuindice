package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable

/** "Esta semana": the class in progress or next, then the evaluations that have not gone by. */
@Immutable
data class SummaryWeekItem(
	val classRow: SummaryClassRowItem?,
	val evaluations: List<SummaryEvaluationRowItem>
)
