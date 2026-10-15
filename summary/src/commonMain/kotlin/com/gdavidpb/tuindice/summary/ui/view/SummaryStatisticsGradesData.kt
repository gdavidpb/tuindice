package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsGradesItem
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsBarChart
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsChartLabels
import com.gdavidpb.tuindice.summary.ui.chart.toStatisticsBarPoints

// The histogram of the grades, one bar for each grade, with the figure of the attempts that carry no grade under it.
@Composable
fun ColumnScope.SummaryStatisticsGradesData(
	data: SummaryStatisticsGradesItem.Content.Data,
	summary: String,
	chartActionLabel: String
) {
	val bars = data.bars.toStatisticsBarPoints()

	key(bars) {
		StatisticsBarChart(
			bars = bars,
			maxValue = null,
			reference = null,
			labels = StatisticsChartLabels(
				key = SummaryStatisticsChartKey.Grades,
				summary = summary,
				actionLabel = chartActionLabel
			)
		)
	}

	SummaryStatisticsFactsView(facts = data.facts)
}
