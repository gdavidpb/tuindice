package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag

// The fade that marks one edge of a scroll frame: a band of the card's surface, painted with [brush] along the edge that
// [alignment] names, and kept under the [tag] of that edge.
@Composable
fun BoxScope.StatisticsEdgeFade(
	alignment: Alignment,
	brush: Brush,
	tag: String
) {
	Box(
		modifier = Modifier
			.align(alignment)
			.fillMaxHeight()
			.width(StatisticsChartDefaults.EdgeFadeWidth)
			.background(brush)
			.testTag(tag)
	)
}
