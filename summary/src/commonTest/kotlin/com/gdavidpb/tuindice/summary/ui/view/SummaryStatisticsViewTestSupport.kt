package com.gdavidpb.tuindice.summary.ui.view

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFigureItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsOutcome
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsSegmentItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsTooltipItem

// The words of a view test are raw: the view writes them exactly as they are given.
internal fun words(value: String): UiText = UiText.Raw(value)

internal fun figureOf(value: String, label: String, trailing: String? = null): SummaryStatisticsFigureItem {
	return SummaryStatisticsFigureItem(
		value = value,
		label = words(label),
		trailing = trailing?.let(::words)
	)
}

internal fun tooltipOf(title: String, vararg lines: String): SummaryStatisticsTooltipItem {
	return SummaryStatisticsTooltipItem(
		title = words(title),
		lines = lines.map(::words)
	)
}

internal fun segmentOf(
	outcome: SummaryStatisticsOutcome,
	value: Float,
	legend: String,
	title: String
): SummaryStatisticsSegmentItem {
	return SummaryStatisticsSegmentItem(
		outcome = outcome,
		value = value,
		legend = words(legend),
		tooltip = tooltipOf(title)
	)
}
