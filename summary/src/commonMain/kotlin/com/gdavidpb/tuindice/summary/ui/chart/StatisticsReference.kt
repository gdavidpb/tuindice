package com.gdavidpb.tuindice.summary.ui.chart

/**
 * A reference line across a [StatisticsBarChart] at [value] on its scale. [label] is written at the end of the line,
 * above it and clear of the edge fade.
 */
data class StatisticsReference(
	val value: Float,
	val label: String
)
