package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.summary.presentation.mapper.toRichText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsSubjectsItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsChartLabels
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsSegment
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsStackedBar
import com.gdavidpb.tuindice.summary.ui.model.SummaryStatisticsBlockHead

/**
 * "Materias": the subjects of the closed terms by outcome, as one stacked bar with its legend. The figure counts every
 * subject; the bar holds the approved, the failed and the withdrawn, and the subjects without a result are a compact
 * figure among the facts, which also say how many were approved on the first attempt and how many were repeated.
 */
@Composable
fun SummaryStatisticsSubjectsView(
	item: SummaryStatisticsSubjectsItem,
	chartActionLabel: String,
	modifier: Modifier = Modifier
) {
	SummaryStatisticsBlockView(
		head = SummaryStatisticsBlockHead(
			tag = SummaryUiTags.StatisticsSubjectsBlock,
			title = item.title.asString()
		),
		modifier = modifier
	) {
		when (val content = item.content) {
			is SummaryStatisticsSubjectsItem.Content.Empty -> SummaryStatisticsEmptyView(
				message = content.text.asString()
			)

			is SummaryStatisticsSubjectsItem.Content.Data -> SubjectsData(
				data = content,
				chartActionLabel = chartActionLabel
			)
		}
	}
}

@Composable
private fun ColumnScope.SubjectsData(
	data: SummaryStatisticsSubjectsItem.Content.Data,
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
				key = SummaryStatisticsChartKey.Subjects,
				summary = data.chartSummary.asString(),
				actionLabel = chartActionLabel
			)
		)
	}

	SummaryStatisticsLegendView(segments = segments)

	SummaryStatisticsFactsView(facts = data.facts)
}
