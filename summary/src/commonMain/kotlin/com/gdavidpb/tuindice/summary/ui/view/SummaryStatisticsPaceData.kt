package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsPaceItem
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsBarChart
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsChartLabels
import com.gdavidpb.tuindice.summary.ui.chart.toStatisticsBarPoints
import com.gdavidpb.tuindice.summary.ui.chart.toStatisticsReference

// The figure of the pace, the bars of the closed terms under their reference line, and the projection under the chart.
@Composable
fun ColumnScope.SummaryStatisticsPaceData(
	data: SummaryStatisticsPaceItem.Content.Data,
	summary: String,
	chartActionLabel: String
) {
	val bars = data.bars.toStatisticsBarPoints()
	val reference = data.reference.toStatisticsReference()

	SummaryStatisticsFigureView(figure = data.figure)

	key(bars) {
		StatisticsBarChart(
			bars = bars,
			maxValue = null,
			reference = reference,
			labels = StatisticsChartLabels(
				key = SummaryStatisticsChartKey.Pace,
				summary = summary,
				actionLabel = chartActionLabel
			)
		)
	}

	SummaryStatisticsPaceProjection(projection = data.projection)
}
