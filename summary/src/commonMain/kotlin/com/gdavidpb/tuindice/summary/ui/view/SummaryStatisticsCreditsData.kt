package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.view.TuIndiceProgressBar
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCreditsItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsChartLabels
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsStackedBar
import com.gdavidpb.tuindice.summary.ui.chart.toStatisticsSegments

// The credits of the closed terms by outcome: their figure and the stacked bar with its legend. When a pensum is cached,
// its own progress follows the bar, with its share and a track in the approved colour.
@Composable
fun ColumnScope.SummaryStatisticsCreditsData(
	data: SummaryStatisticsCreditsItem.Content.Data,
	chartActionLabel: String
) {
	val segments = data.segments

	SummaryStatisticsFigureView(figure = data.figure)

	key(segments) {
		StatisticsStackedBar(
			segments = segments.toStatisticsSegments(),
			labels = StatisticsChartLabels(
				key = SummaryStatisticsChartKey.Credits,
				summary = data.chartSummary.asString(),
				actionLabel = chartActionLabel
			)
		)
	}

	SummaryStatisticsLegendView(segments = segments)

	data.pensum?.let { pensum ->
		HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

		SummaryStatisticsFigureView(figure = pensum.figure)

		TuIndiceProgressBar(
			fraction = pensum.fraction,
			color = AcademicStatusColors.approved(),
			modifier = Modifier.testTag(SummaryUiTags.StatisticsPensumTrack),
			fillTag = SummaryUiTags.StatisticsTrackFill
		)
	}
}
