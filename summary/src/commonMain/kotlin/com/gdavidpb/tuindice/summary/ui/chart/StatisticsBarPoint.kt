package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.ui.model.RichTooltipText

/**
 * A bar of a [StatisticsBarChart]: its [value] on the chart's scale, its [label] for a screen reader, the [axisLabel]
 * written under its column and the [tooltip] it shows when it is open. A bar may also carry a [valueLabel], the figure
 * written just above it, which climbs with the bar as the bar grows.
 */
@Immutable
data class StatisticsBarPoint(
	val label: String,
	val value: Float,
	val tooltip: RichTooltipText,
	val axisLabel: String,
	val valueLabel: String? = null
)
