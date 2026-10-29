package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * The line that offers a term report the student has not opened: its [title] names the term, its [subtitle] is what the
 * report says of itself, and the line leads to the report, [target].
 */
@Immutable
data class SummaryTermReportItem(
	val insightId: String,
	val title: UiText,
	val subtitle: String,
	val target: SummaryHubTarget.TermReport
)
