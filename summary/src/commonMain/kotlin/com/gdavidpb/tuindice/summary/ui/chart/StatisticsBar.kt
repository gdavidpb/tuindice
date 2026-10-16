package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * The bar of one point of a bar chart: a column that stands on the baseline and rises to [shownFraction] of the plot,
 * rounded at its top and [width] wide, in [color]. The bar keeps the [tag] of its point, so a test finds it.
 */
@Composable
fun BoxScope.StatisticsBar(
	shownFraction: Float,
	width: Float,
	color: Color,
	tag: String
) {
	Box(
		modifier = Modifier
			.align(Alignment.BottomCenter)
			.width(width.dp)
			.height((StatisticsChartDefaults.PlotHeight.value * shownFraction).dp)
			.background(
				color = color,
				shape = RoundedCornerShape(
					topStart = StatisticsChartDefaults.BarCornerRadius,
					topEnd = StatisticsChartDefaults.BarCornerRadius
				)
			)
			.testTag(tag)
	)
}
