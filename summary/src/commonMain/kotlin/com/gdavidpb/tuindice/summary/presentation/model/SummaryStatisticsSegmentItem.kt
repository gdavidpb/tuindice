package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * A segment of a stacked bar: its [outcome] (the screen gives it its color), its [value] on the bar, its [legend] and
 * the [tooltip] it shows when it is open.
 */
@Immutable
data class SummaryStatisticsSegmentItem(
	val outcome: SummaryStatisticsOutcome,
	val value: Float,
	val legend: UiText,
	val tooltip: SummaryStatisticsTooltipItem
)
