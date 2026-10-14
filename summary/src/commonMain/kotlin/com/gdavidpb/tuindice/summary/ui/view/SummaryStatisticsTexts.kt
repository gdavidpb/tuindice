package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.view.RichTooltipText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFigureItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsTooltipItem

// The tooltip of a point as its chart shows it: the title, and the lines under it.
@Composable
internal fun SummaryStatisticsTooltipItem.toRichText(): RichTooltipText {
	return RichTooltipText(
		title = title.asString(),
		lines = lines.map { line -> line.asString() }
	)
}

// The figure as a screen reader says it, its value and what it counts, as one phrase: "4.1250 acumulado".
@Composable
internal fun SummaryStatisticsFigureItem.summaryText(): String = "$value ${label.asString()}"
