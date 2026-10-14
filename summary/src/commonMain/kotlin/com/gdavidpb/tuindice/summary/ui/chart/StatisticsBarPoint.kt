package com.gdavidpb.tuindice.summary.ui.chart

import com.gdavidpb.tuindice.base.ui.view.RichTooltipText

/**
 * A bar of a [StatisticsBarChart]: its [value] on the chart's scale, its [label] for a screen reader and the
 * [tooltip] it shows when it is open.
 */
data class StatisticsBarPoint(
	val label: String,
	val value: Float,
	val tooltip: RichTooltipText
)
