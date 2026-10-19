package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

// The dashed line of a reference across a bar chart, at [y] and [widthDp] long, with its [label] written at the end of
// the line, above it and on the card's own surface, so a bar that rises behind the label never hides its words.
@Composable
fun BoxScope.StatisticsReferenceLine(
	label: String?,
	y: Float,
	widthDp: Float
) {
	val color = MaterialTheme.colorScheme.onSurfaceVariant

	Canvas(modifier = Modifier.matchParentSize()) {
		val lineY = y.dp.toPx()

		drawLine(
			color = color,
			start = Offset(x = 0f, y = lineY),
			end = Offset(x = widthDp.dp.toPx(), y = lineY),
			strokeWidth = 1.dp.toPx(),
			pathEffect = PathEffect.dashPathEffect(
				intervals = floatArrayOf(
					StatisticsChartDefaults.ReferenceDashLength.toPx(),
					StatisticsChartDefaults.ReferenceGapLength.toPx()
				)
			)
		)
	}

	if (label == null) return

	Text(
		modifier = Modifier
			.align(Alignment.TopEnd)
			.offset(y = (y - StatisticsChartDefaults.ReferenceLabelHeight.value).dp)
			.padding(end = StatisticsChartDefaults.EdgeFadeWidth)
			// On the card's own surface, so a bar that rises behind the label never hides its words.
			.background(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(4.dp))
			.padding(horizontal = 4.dp)
			.testTag(SummaryUiTags.StatisticsReferenceLabel),
		text = label,
		style = MaterialTheme.typography.labelSmall,
		color = color
	)
}
