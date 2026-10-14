package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsIndexItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsChartLabels
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsLineChart
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsLinePoint
import com.gdavidpb.tuindice.summary.ui.model.SummaryStatisticsBlockHead

/**
 * "Índice": the cumulative average of the closed terms as a line, with the index of each term as its marker and each
 * term named under its column. Its figure is the cumulative average at the last closed term, which the caption names.
 * The best and the worst regular terms are written under the chart.
 */
@Composable
fun SummaryStatisticsIndexView(
	item: SummaryStatisticsIndexItem,
	chartActionLabel: String,
	modifier: Modifier = Modifier
) {
	SummaryStatisticsBlockView(
		head = SummaryStatisticsBlockHead(
			tag = SummaryUiTags.StatisticsIndexBlock,
			title = item.title.asString()
		),
		modifier = modifier
	) {
		when (val content = item.content) {
			is SummaryStatisticsIndexItem.Content.Empty -> SummaryStatisticsEmptyView(
				message = content.text.asString()
			)

			is SummaryStatisticsIndexItem.Content.Data -> IndexData(
				data = content,
				chartActionLabel = chartActionLabel
			)
		}
	}
}

@Composable
private fun ColumnScope.IndexData(
	data: SummaryStatisticsIndexItem.Content.Data,
	chartActionLabel: String
) {
	val figure = data.figure
	val points = data.points.map { point ->
		StatisticsLinePoint(
			label = point.tooltip.title.asString(),
			lineValue = point.lineValue,
			tooltip = point.tooltip.toRichText(),
			axisLabel = point.axisLabel.asString(),
			markerValue = point.markerValue
		)
	}
	val notes = listOfNotNull(data.best, data.worst)

	SummaryStatisticsFigureView(figure = figure)

	Text(
		modifier = Modifier.testTag(SummaryUiTags.StatisticsCaption),
		text = data.caption.asString(),
		style = MaterialTheme.typography.bodySmall,
		color = MaterialTheme.colorScheme.onSurfaceVariant
	)

	key(points) {
		StatisticsLineChart(
			points = points,
			minValue = data.minValue,
			maxValue = data.maxValue,
			labels = StatisticsChartLabels(
				key = SummaryStatisticsChartKey.Index,
				summary = figure.summaryText(),
				actionLabel = chartActionLabel
			)
		)
	}

	if (notes.isNotEmpty()) {
		SummaryStatisticsNotesView(notes = notes)
	}
}
