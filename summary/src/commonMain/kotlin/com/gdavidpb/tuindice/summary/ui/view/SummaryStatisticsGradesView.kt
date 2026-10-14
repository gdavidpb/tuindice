package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsGradesItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsBarChart
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsBarPoint
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsChartLabels
import com.gdavidpb.tuindice.summary.ui.model.SummaryStatisticsBlockHead

/**
 * "Tus notas": one bar for each grade from 1 to 5, with the attempts that carry it written above the bar and
 * the grade under it. The notes name the most frequent grade and say what the record did not report, when anything.
 */
@Composable
fun SummaryStatisticsGradesView(
	item: SummaryStatisticsGradesItem,
	chartActionLabel: String,
	modifier: Modifier = Modifier
) {
	val title = item.title.asString()

	SummaryStatisticsBlockView(
		head = SummaryStatisticsBlockHead(
			tag = SummaryUiTags.StatisticsGradesBlock,
			title = title
		),
		modifier = modifier
	) {
		when (val content = item.content) {
			is SummaryStatisticsGradesItem.Content.Empty -> SummaryStatisticsEmptyView(
				message = content.text.asString()
			)

			is SummaryStatisticsGradesItem.Content.Data -> GradesData(
				data = content,
				summary = title,
				chartActionLabel = chartActionLabel
			)
		}
	}
}

@Composable
private fun ColumnScope.GradesData(
	data: SummaryStatisticsGradesItem.Content.Data,
	summary: String,
	chartActionLabel: String
) {
	val bars = data.bars.map { bar ->
		StatisticsBarPoint(
			label = bar.tooltip.title.asString(),
			value = bar.value,
			tooltip = bar.tooltip.toRichText(),
			axisLabel = bar.axisLabel.asString(),
			valueLabel = bar.valueLabel?.asString()
		)
	}
	val notes = data.notes + listOfNotNull(data.unreported)

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

	SummaryStatisticsNotesView(notes = notes)
}
