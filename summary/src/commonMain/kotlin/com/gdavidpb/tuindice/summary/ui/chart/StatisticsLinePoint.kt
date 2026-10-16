package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.gdavidpb.tuindice.base.ui.model.RichTooltipText

/**
 * A point of a [StatisticsLineChart]. The line runs through [lineValue], and the point's marker sits on [markerValue],
 * which is the line's value unless it says so. [label] names the point for a screen reader, [tooltip] is what the
 * point shows when it is open, and [axisLabel] is written under its column. A [ringColor] draws a ring around the
 * marker, for the one point the chart's block names apart.
 */
@Immutable
data class StatisticsLinePoint(
	val label: String,
	val lineValue: Float,
	val tooltip: RichTooltipText,
	val axisLabel: String,
	val markerValue: Float = lineValue,
	val ringColor: Color? = null
)
