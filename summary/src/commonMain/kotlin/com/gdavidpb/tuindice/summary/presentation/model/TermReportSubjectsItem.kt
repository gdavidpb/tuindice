package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/** The subjects of the term: the credits it approved, the count of each outcome, and one row per subject. */
@Immutable
data class TermReportSubjectsItem(
	val credits: UiText,
	val legend: List<TermReportLegendItem>,
	val rows: List<TermReportSubjectItem>
)
