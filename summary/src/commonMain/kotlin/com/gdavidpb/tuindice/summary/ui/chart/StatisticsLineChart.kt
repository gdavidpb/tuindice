package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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

					LineTrace(points = points, range = range, columnWidth = columnWidth)

				LineMarkers(
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

@Composable
private fun BoxScope.LineTrace(
	points: List<StatisticsLinePoint>,
	range: ClosedFloatingPointRange<Float>,
	columnWidth: Float
) {
	val dataColor = StatisticsChartDefaults.dataColor()

	Canvas(modifier = Modifier.matchParentSize()) {
		val path = Path()

		points.forEachIndexed { index, point ->
			val x = StatisticsChartGeometry.columnCenter(index, columnWidth).dp.toPx()
			val y = StatisticsChartGeometry.yOf(
				fraction = StatisticsChartGeometry.valueFraction(point.lineValue, range.start, range.endInclusive),
				topDp = StatisticsChartDefaults.LineInset.toPx(),
				heightDp = StatisticsChartDefaults.LineBandHeight.toPx()
			)

			if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
		}

		drawPath(
			path = path,
			color = dataColor,
			style = Stroke(width = StatisticsChartDefaults.LineStrokeWidth.toPx())
		)
	}
}

@Composable
private fun BoxScope.LineMarkers(
	points: List<StatisticsLinePoint>,
	range: ClosedFloatingPointRange<Float>,
	columnWidth: Float,
	labels: StatisticsChartLabels,
	openIndex: MutableState<Int?>
) {
	val dataColor = StatisticsChartDefaults.dataColor()
	val hasSelection = openIndex.value != null
	val half = StatisticsChartDefaults.TargetMinSize.value / 2

	points.forEachIndexed { index, point ->
		key(index) {
			val isOpen = openIndex.value == index
			val centerX = StatisticsChartGeometry.columnCenter(index, columnWidth)
			val centerY = StatisticsChartGeometry.yOf(
				fraction = StatisticsChartGeometry.valueFraction(point.markerValue, range.start, range.endInclusive),
				topDp = StatisticsChartDefaults.LineInset.value,
				heightDp = StatisticsChartDefaults.LineBandHeight.value
			)

			Box(
				modifier = Modifier
					.offset(x = (centerX - half).dp, y = (centerY - half).dp)
					.size(StatisticsChartDefaults.TargetMinSize)
			) {
				StatisticsTooltipTarget(
					target = labels.targetOf(index, point.label, point.tooltip),
					isOpen = isOpen,
					onOpenChange = { open -> openIndex.value = if (open) index else null }
				) {
					Box(
						modifier = Modifier
							.size(StatisticsChartDefaults.MarkerSize)
							.background(
								color = dataColor.copy(
									alpha = StatisticsChartDefaults.alphaOf(isOpen, hasSelection)
								),
								shape = CircleShape
							)
					)
				}
			}
		}
	}
}
