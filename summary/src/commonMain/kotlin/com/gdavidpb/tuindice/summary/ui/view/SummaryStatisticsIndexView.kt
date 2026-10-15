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
import com.gdavidpb.tuindice.summary.ui.chart.toStatisticsLinePoints
import com.gdavidpb.tuindice.summary.ui.model.SummaryStatisticsBlockHead

/**
 * "Índice": the cumulative average of the closed terms as a line, with the index of each term as its marker and each
 * term named under its column. Its figure is the cumulative average at the last closed term, written as the header
 * writes a grade, and the caption names that term. The best and the worst regular terms are written under the chart,
 * each beside a ring in the colour of its point.
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
	val bestColor = AcademicStatusColors.approved()
	val worstColor = MaterialTheme.colorScheme.error
	val label = data.label.asString()
	val points = data.points.toStatisticsLinePoints()

	SummaryGradeFigureView(
		grade = data.grade,
		label = label,
		style = MaterialTheme.typography.headlineMedium,
		tag = SummaryUiTags.StatisticsFigure
	)

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
				summary = data.chartSummary.asString(),
				actionLabel = chartActionLabel
			)
		)
	}

	data.best?.let { best ->
		RankedTermNote(text = best.asString(), color = bestColor, tag = SummaryUiTags.StatisticsBestTerm)
	}

	data.worst?.let { worst ->
		RankedTermNote(text = worst.asString(), color = worstColor, tag = SummaryUiTags.StatisticsWorstTerm)
	}
}

// "◯ Mejor: Ene - Mar 2024 · 4.5714" and "◯ Peor: …": the ring before the words is the one around that point
// on the chart, in the same colour.
@Composable
private fun RankedTermNote(
	text: String,
	color: Color,
	tag: String
) {
	Row(
		modifier = Modifier.testTag(tag),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
	) {
		Box(
			modifier = Modifier
				.size(NoteRingSize)
				.border(width = StatisticsChartDefaults.RingStrokeWidth, color = color, shape = CircleShape)
		)

		Text(
			text = text,
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}

private val NoteRingSize = 10.dp
