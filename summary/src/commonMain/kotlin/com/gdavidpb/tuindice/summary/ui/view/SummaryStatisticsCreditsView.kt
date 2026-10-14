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
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCreditsItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsChartLabels
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsSegment
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsStackedBar
import com.gdavidpb.tuindice.summary.ui.model.SummaryStatisticsBlockHead

/**
 * "Créditos": the credits of the closed terms by outcome, as one stacked bar with its legend. When a pensum is cached,
 * its own progress follows the bar: the approved credits out of the pensum's total, with their share and a track in the
 * approved colour.
 */
@Composable
fun SummaryStatisticsCreditsView(
	item: SummaryStatisticsCreditsItem,
	chartActionLabel: String,
	modifier: Modifier = Modifier
) {
	SummaryStatisticsBlockView(
		head = SummaryStatisticsBlockHead(
			tag = SummaryUiTags.StatisticsCreditsBlock,
			title = item.title.asString()
		),
		modifier = modifier
	) {
		when (val content = item.content) {
			is SummaryStatisticsCreditsItem.Content.Empty -> SummaryStatisticsEmptyView(
				message = content.text.asString()
			)

			is SummaryStatisticsCreditsItem.Content.Data -> CreditsData(
				data = content,
				chartActionLabel = chartActionLabel
			)
		}
	}
}

@Composable
private fun ColumnScope.CreditsData(
	data: SummaryStatisticsCreditsItem.Content.Data,
	chartActionLabel: String
) {
	val segments = data.segments

	SummaryStatisticsFigureView(figure = data.figure)

	key(segments) {
		StatisticsStackedBar(
			segments = segments.map { segment ->
				StatisticsSegment(
					label = segment.legend.asString(),
					value = segment.value,
					color = segment.outcome.color(),
					tooltip = segment.tooltip.toRichText()
				)
			},
			labels = StatisticsChartLabels(
				key = SummaryStatisticsChartKey.Credits,
				summary = data.figure.summaryText(),
				actionLabel = chartActionLabel
			)
		)
	}

	SummaryStatisticsLegendView(segments = segments)

	data.pensum?.let { pensum ->
		HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

		SummaryStatisticsFigureView(figure = pensum.figure)

		SummaryStatisticsTrackView(
			fraction = pensum.fraction,
			color = AcademicStatusColors.approved(),
			modifier = Modifier.testTag(SummaryUiTags.StatisticsPensumTrack)
		)
	}
}
