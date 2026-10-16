package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

// The line through the points of a line chart: each point's line value, in its column, drawn in the data colour over
// the band of the chart. The line is decoration beside the markers, so it takes no touch and no tag.
@Composable
fun BoxScope.StatisticsLineTrace(
	points: List<StatisticsLinePoint>,
	range: ClosedFloatingPointRange<Float>,
	columnWidth: Float,
	modifier: Modifier = Modifier
) {
	val dataColor = StatisticsChartDefaults.dataColor()

	Canvas(modifier = modifier.matchParentSize()) {
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
