package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

// The figure written just above a bar. The bar stands on the baseline, so its top sits the drawn share of the plot
// above it; the figure's own line lies over that top. It is placed from the same share the bar is drawn with, so it
// climbs with the bar while the bar grows, and that share is read where the figure is placed, not where it is composed.
// The figure of [StatisticsValueLabel] with its share read where the figure is placed, so a frame of the growth moves
// the figure and composes nothing.
@Composable
fun BoxScope.StatisticsValueLabel(
	text: String,
	tag: String,
	shownFraction: () -> Float,
	columnWidth: Float
) {
	Text(
		modifier = Modifier
			.offset {
				val top = StatisticsChartDefaults.ChartHeight.value -
					StatisticsChartDefaults.PlotHeight.value * shownFraction() -
					StatisticsChartDefaults.ValueLabelHeight.value

				IntOffset(x = 0, y = top.dp.roundToPx())
			}
			.width(columnWidth.dp)
			.height(StatisticsChartDefaults.ValueLabelHeight)
			.testTag(tag)
			.semantics { hideFromAccessibility() },
		text = text,
		maxLines = 1,
		overflow = TextOverflow.Clip,
		textAlign = TextAlign.Center,
		style = MaterialTheme.typography.labelSmall,
		color = MaterialTheme.colorScheme.onSurfaceVariant
	)
}
