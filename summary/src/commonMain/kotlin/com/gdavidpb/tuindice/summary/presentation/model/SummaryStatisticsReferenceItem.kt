package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/** A reference line across a chart, at [value] on its scale, with the [label] written at its end. */
@Immutable
data class SummaryStatisticsReferenceItem(
	val value: Float,
	val label: UiText
)
