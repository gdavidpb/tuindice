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
 * A row of bars, one per point, each growing from the baseline to its share of the scale. The scale tops out at
 * [maxValue], or at the largest of the values and the reference when it is absent. A [reference] is a dashed line
 * across the chart, labelled above its end. Each column names its bar under the plot, and a bar that carries a value
 * label shows that figure just above it. Tapping a bar opens its tooltip and closes the one that was open. Past six
 * bars the chart scrolls, labels and all, and it opens at its latest bar.
 */
@Composable
fun StatisticsBarChart(
	bars: List<StatisticsBarPoint>,
	maxValue: Float?,
	reference: StatisticsReference?,
	labels: StatisticsChartLabels,
	modifier: Modifier = Modifier
) {
	val openIndex = remember { mutableStateOf<Int?>(null) }
	val scale = maxValue ?: StatisticsChartGeometry.scaleOf(bars.map { bar -> bar.value }, reference?.value)

	BoxWithConstraints(
		modifier = modifier
			.fillMaxWidth()
			.testTag(SummaryUiTags.statisticsChart(labels.key))
			.semantics { contentDescription = labels.summary }
	) {
		val columnWidth = StatisticsChartGeometry.columnWidth(maxWidth.value, bars.size)
		val contentWidth = StatisticsChartGeometry.contentWidth(columnWidth, bars.size)

		StatisticsScrollFrame {
			Box(modifier = Modifier.width(contentWidth.dp).height(StatisticsChartDefaults.ContentHeight)) {
				StatisticsBarColumns(
					bars = bars,
					scale = scale,
					columnWidth = columnWidth,
					labels = labels,
					openIndex = openIndex
				)

				StatisticsAxisLabels(
					key = labels.key,
					labels = bars.map { bar -> bar.axisLabel },
					columnWidth = columnWidth
				)

				reference?.let { line ->
					StatisticsReferenceLine(
						label = line.label,
						y = StatisticsChartGeometry.yOf(
							fraction = StatisticsChartGeometry.barFraction(line.value, scale),
							topDp = StatisticsChartDefaults.PlotTop.value,
							heightDp = StatisticsChartDefaults.PlotHeight.value
						),
						widthDp = contentWidth
					)
				}
			}
		}
	}
}
