package com.gdavidpb.tuindice.summary.ui.chart

// The tags of the words a chart writes beside its points: the label under each column, and the figure above a bar. They
// are named by the chart's key and the index of the point, as the tags of its touch targets and fills are.
object StatisticsLabelTags {
	fun axis(key: String, index: Int): String = "summary_statistics_${key}_axis_$index"

	fun value(key: String, index: Int): String = "summary_statistics_${key}_value_$index"
}
