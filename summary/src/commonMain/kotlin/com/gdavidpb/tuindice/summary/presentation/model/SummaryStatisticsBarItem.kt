package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * A bar of a chart: its [axisLabel] under it, its [value] on the chart's scale and the [tooltip] it shows when open.
 * A bar may also carry a [valueLabel], the figure written above it, as the histogram of the grades does.
 */
@Immutable
data class SummaryStatisticsBarItem(
	val axisLabel: UiText,
	val value: Float,
	val tooltip: SummaryStatisticsTooltipItem,
	val valueLabel: UiText? = null,
	val isHighlighted: Boolean = false
)
