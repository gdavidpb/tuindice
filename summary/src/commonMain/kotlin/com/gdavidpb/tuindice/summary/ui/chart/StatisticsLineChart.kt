package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

/**
 * A line through the points of a term's history, with a button on each point. The line runs through each point's line
 * value, and the marker of the point sits on its marker value, so the index of a term can mark its own value over the
 * cumulative line. Tapping a point opens its tooltip and closes the one that was open. Past six points the chart
 * scrolls, labels and all, and it opens at its latest point. Each point is named under its column by its axis label.
 */
@Composable
fun StatisticsLineChart(
	points: List<StatisticsLinePoint>,
	minValue: Float,
	maxValue: Float,
	labels: StatisticsChartLabels,
	modifier: Modifier = Modifier
) {
	val openIndex = remember { mutableStateOf<Int?>(null) }
	val range = minValue..maxValue

	BoxWithConstraints(
		modifier = modifier
			.fillMaxWidth()
			.testTag(SummaryUiTags.statisticsChart(labels.key))
			.semantics { contentDescription = labels.summary }
	) {
		val columnWidth = StatisticsChartGeometry.columnWidth(maxWidth.value, points.size)
		val contentWidth = StatisticsChartGeometry.contentWidth(columnWidth, points.size)

		StatisticsScrollFrame {
			Box(modifier = Modifier.width(contentWidth.dp).height(StatisticsChartDefaults.ContentHeight)) {
				StatisticsAxisLabels(
					key = labels.key,
					labels = points.map { point -> point.axisLabel },
					columnWidth = columnWidth
				)

				StatisticsLineTrace(points = points, range = range, columnWidth = columnWidth)

				StatisticsLineMarkers(
					points = points,
					range = range,
					columnWidth = columnWidth,
					labels = labels,
					openIndex = openIndex
				)
			}
		}
	}
}
