package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsIndexItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsChartLabels
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsLineChart
import com.gdavidpb.tuindice.summary.ui.chart.toStatisticsLinePoints

// The grade of the index, its caption and its chart, with the best and the worst regular terms named under it.
@Composable
fun ColumnScope.SummaryStatisticsIndexData(
	data: SummaryStatisticsIndexItem.Content.Data,
	chartActionLabel: String
) {
	val bestColor = AcademicStatusColors.approved()
	val worstColor = MaterialTheme.colorScheme.error
	val points = data.points.toStatisticsLinePoints()

	SummaryGradeFigureView(
		grade = data.grade,
		label = data.label.asString(),
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
		SummaryStatisticsRankedTermNote(text = best.asString(), color = bestColor, tag = SummaryUiTags.StatisticsBestTerm)
	}

	data.worst?.let { worst ->
		SummaryStatisticsRankedTermNote(text = worst.asString(), color = worstColor, tag = SummaryUiTags.StatisticsWorstTerm)
	}
}
