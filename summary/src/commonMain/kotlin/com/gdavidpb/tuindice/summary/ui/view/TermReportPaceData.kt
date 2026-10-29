package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.summary.presentation.model.TermReportPaceItem
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsBarChart
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsChartLabels
import com.gdavidpb.tuindice.summary.ui.chart.toStatisticsBarPoints
import com.gdavidpb.tuindice.summary.ui.chart.toStatisticsReference

// The bars under their average, and under the chart what the term approved beside it.
@Composable
fun TermReportPaceData(
	pace: TermReportPaceItem.Data,
	title: String,
	chartActionLabel: String
) {
	val bars = pace.bars.toStatisticsBarPoints()

	key(bars) {
		StatisticsBarChart(
			bars = bars,
			maxValue = null,
			reference = pace.reference.toStatisticsReference(),
			labels = StatisticsChartLabels(
				key = SummaryStatisticsChartKey.TermReportPaceChart,
				summary = title,
				actionLabel = chartActionLabel
			)
		)
	}

	TermReportPaceLegend(
		current = pace.currentLegend.asString(),
		average = pace.averageLegend.asString()
	)
}
