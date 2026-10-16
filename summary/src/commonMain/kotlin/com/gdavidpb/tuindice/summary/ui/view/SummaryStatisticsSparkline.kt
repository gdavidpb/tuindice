package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsChartDefaults
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsChartGeometry

// The cumulative averages in order, as a thin line scaled from the lowest of them to the highest, in the colour of the
// data. It is decoration: a screen reader does not read it. A single average is drawn as a dot.
@Composable
fun SummaryStatisticsSparkline(values: List<Float>) {
	val color = StatisticsChartDefaults.dataColor()
	val minValue = values.minOrNull() ?: 0f
	val maxValue = values.maxOrNull() ?: 0f

	Canvas(
		modifier = Modifier
			.testTag(SummaryUiTags.StatisticsSparkline)
			.fillMaxWidth()
			.height(SparklineHeight)
			.clearAndSetSemantics { }
	) {
		// The line keeps half its width clear of the edges, so its ends are not cut.
		val strokePx = StatisticsChartDefaults.LineStrokeWidth.toPx()
		val bandHeight = size.height - strokePx * 2

		val points = values.mapIndexed { index, value ->
			val x = if (values.size == 1) size.width / 2 else size.width * index / (values.size - 1)
			val fraction = StatisticsChartGeometry.valueFraction(value = value, minValue = minValue, maxValue = maxValue)

			Offset(x = x, y = strokePx + bandHeight * (1f - fraction))
		}

		if (points.size == 1) {
			drawCircle(
				color = color,
				radius = StatisticsChartDefaults.MarkerSize.toPx() / 2,
				center = points.single()
			)
		} else {
			val path = Path()

			points.forEachIndexed { index, point ->
				if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
			}

			drawPath(
				path = path,
				color = color,
				style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
			)
		}
	}
}

private val SparklineHeight = 44.dp
