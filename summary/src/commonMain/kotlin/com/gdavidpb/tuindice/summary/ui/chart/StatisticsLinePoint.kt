package com.gdavidpb.tuindice.summary.ui.chart

import com.gdavidpb.tuindice.base.ui.view.RichTooltipText

/**
 * A point of a [StatisticsLineChart]. The line runs through [lineValue], and the point's marker sits on [markerValue],
 * which is the line's value unless it says so. [label] names the point for a screen reader, [tooltip] is what the
 * point shows when it is open, and [axisLabel] is written under its column.
 */
data class StatisticsLinePoint(
	val label: String,
	val lineValue: Float,
	val tooltip: RichTooltipText,
	val axisLabel: String,
	val markerValue: Float = lineValue
)
