package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsIndexItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsChartDefaults
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
	val bestColor = AcademicStatusColors.approved()
	val points = data.points.map { point ->
		StatisticsLinePoint(
			label = point.tooltip.title.asString(),
			lineValue = point.lineValue,
			tooltip = point.tooltip.toRichText(),
			axisLabel = point.axisLabel.asString(),
			markerValue = point.markerValue,
			ringColor = if (point.isBest) bestColor else null
		)
	}

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

	data.best?.let { best -> BestTermNote(text = best.asString(), color = bestColor) }

	data.worst?.let { worst -> SummaryStatisticsNotesView(notes = listOf(worst)) }
}

// "◯ Mejor: Ene - Mar 2024 · 4.5714": the ring before the words is the one around that term's point on the chart.
@Composable
private fun BestTermNote(text: String, color: Color) {
	Row(
		modifier = Modifier.testTag(SummaryUiTags.StatisticsBestTerm),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
	) {
		Box(
			modifier = Modifier
				.size(BestRingSize)
				.border(width = StatisticsChartDefaults.RingStrokeWidth, color = color, shape = CircleShape)
		)

		Text(
			text = text,
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}

private val BestRingSize = 10.dp
