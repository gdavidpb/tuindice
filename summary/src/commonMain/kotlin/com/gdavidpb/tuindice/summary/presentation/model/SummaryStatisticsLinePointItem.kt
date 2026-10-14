package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * A point of the index chart: its [axisLabel] under it, the [lineValue] the cumulative line runs through and the
 * [markerValue] where the point sits, the index of its term. [tooltip] is what it shows when it is open. [isBest]
 * marks the best regular term, the one the block names under the chart, which the chart rings.
 */
@Immutable
data class SummaryStatisticsLinePointItem(
	val axisLabel: UiText,
	val lineValue: Float,
	val markerValue: Float,
	val tooltip: SummaryStatisticsTooltipItem,
	val isBest: Boolean = false
)
