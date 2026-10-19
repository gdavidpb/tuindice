package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.runtime.Immutable

/**
 * A reference line across a [StatisticsBarChart] at [value] on its scale. [label], when there is one, is written at the
 * end of the line, above it and clear of the edge fade.
 */
@Immutable
data class StatisticsReference(
	val value: Float,
	val label: String?
)
