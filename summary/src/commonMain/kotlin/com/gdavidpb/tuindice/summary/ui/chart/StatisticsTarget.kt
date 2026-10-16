package com.gdavidpb.tuindice.summary.ui.chart

import com.gdavidpb.tuindice.base.ui.model.RichTooltipText
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

/**
 * The touch area of one point: its [tag], the [description] and [actionLabel] a screen reader hears for it, and the
 * [tooltip] it shows when it is open.
 */
data class StatisticsTarget(
	val tag: String,
	val description: String,
	val actionLabel: String,
	val tooltip: RichTooltipText
)

// The target of the point at [index] of the chart, named by the chart's key. Its tooltip carries the tag of that point,
// so a test sees which point the tooltip belongs to.
internal fun StatisticsChartLabels.targetOf(
	index: Int,
	description: String,
	tooltip: RichTooltipText
): StatisticsTarget {
	return StatisticsTarget(
		tag = SummaryUiTags.statisticsTarget(key, index),
		description = description,
		actionLabel = actionLabel,
		tooltip = tooltip.copy(testTag = StatisticsTooltipUiTags.point(key, index))
	)
}
