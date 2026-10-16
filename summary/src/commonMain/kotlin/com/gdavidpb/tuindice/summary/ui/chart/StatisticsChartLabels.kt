package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.runtime.Immutable

/**
 * How a statistics chart is found and described: its [key], which names its test tags, the [summary] a screen reader
 * hears for the whole chart, and the [actionLabel] that tapping one of its points offers.
 */
@Immutable
data class StatisticsChartLabels(
	val key: String,
	val summary: String,
	val actionLabel: String
)
