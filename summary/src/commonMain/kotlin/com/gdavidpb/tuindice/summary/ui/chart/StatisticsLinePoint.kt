package com.gdavidpb.tuindice.summary.ui.chart

import com.gdavidpb.tuindice.base.ui.view.RichTooltipText

/**
 * A point of a [StatisticsLineChart]. The line runs through [lineValue], and the point's marker sits on [markerValue],
 * which is the line's value unless it says otherwise. [label] names the point for a screen reader, and [tooltip] is
 * what the point shows when it is open.
 */
data class StatisticsLinePoint(
	val label: String,
	val lineValue: Float,
	val tooltip: RichTooltipText,
	val markerValue: Float = lineValue
)
