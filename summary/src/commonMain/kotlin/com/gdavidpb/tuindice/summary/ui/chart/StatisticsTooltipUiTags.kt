package com.gdavidpb.tuindice.summary.ui.chart

/**
 * The tags of the tooltips that the points of the Statistics charts open. They live apart from `SummaryUiTags`, which
 * already holds as many functions as its detekt threshold allows.
 */
object StatisticsTooltipUiTags {
	/** The tooltip of the point at [index] of the chart named [key], named as the point's touch target is. */
	fun point(key: String, index: Int): String = "summary_statistics_${key}_tooltip_$index"
}
