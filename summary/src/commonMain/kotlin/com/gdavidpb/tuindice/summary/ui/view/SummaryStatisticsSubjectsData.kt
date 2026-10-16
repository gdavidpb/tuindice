package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsSubjectsItem
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsChartLabels
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsStackedBar
import com.gdavidpb.tuindice.summary.ui.chart.toStatisticsSegments

// The subjects of the closed terms: their figure, the stacked bar by outcome with its legend, and the facts under it.
@Composable
fun ColumnScope.SummaryStatisticsSubjectsData(
	data: SummaryStatisticsSubjectsItem.Content.Data,
	chartActionLabel: String
) {
	val segments = data.segments

	SummaryStatisticsFigureView(figure = data.figure)

	key(segments) {
		StatisticsStackedBar(
			segments = segments.toStatisticsSegments(),
			labels = StatisticsChartLabels(
				key = SummaryStatisticsChartKey.Subjects,
				summary = data.chartSummary.asString(),
				actionLabel = chartActionLabel
			)
		)
	}

	SummaryStatisticsLegendView(segments = segments)

	SummaryStatisticsFactsView(facts = data.facts)
}
