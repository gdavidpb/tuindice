package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/** The index of the term and its cumulative index, each with the change from the term before it when there is one. */
@Immutable
data class TermReportHeroItem(
	val title: UiText,
	val index: String?,
	val indexDelta: TermReportDeltaItem?,
	val cumulativeIndex: String?,
	val cumulativeDelta: TermReportDeltaItem?
)
