package com.gdavidpb.tuindice.summary.ui.chart

/**
 * How a statistics chart is found and described: its [key], which names its test tags, the [summary] a screen reader
 * hears for the whole chart, and the [actionLabel] that tapping one of its points offers.
 */
data class StatisticsChartLabels(
	val key: String,
	val summary: String,
	val actionLabel: String
)
