package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

/**
 * Lays [content] out in a horizontal scroll that opens at its latest end, the right one. While more of the content lies
 * beyond a side, a fade in the card's surface marks that edge, so nothing that sits at the edge is read under the fade.
 */
@Composable
fun StatisticsScrollFrame(
	modifier: Modifier = Modifier,
	content: @Composable () -> Unit
) {
	val scrollState = rememberScrollState()
	val latest = scrollState.maxValue

	LaunchedEffect(latest) {
		scrollState.scrollTo(latest)
	}

	// Read through derived states, so scrolling recomposes the frame only when an edge starts or stops hiding content.
	val hidesLeading by remember { derivedStateOf { scrollState.value > 0 } }
	val hidesTrailing by remember { derivedStateOf { scrollState.value < scrollState.maxValue } }
	val surface = MaterialTheme.colorScheme.surfaceVariant

	Box(modifier = modifier) {
		Box(modifier = Modifier.horizontalScroll(scrollState)) {
			content()
		}

		if (hidesLeading) {
			StatisticsEdgeFade(
				alignment = Alignment.CenterStart,
				brush = Brush.horizontalGradient(colors = listOf(surface, Color.Transparent)),
				tag = SummaryUiTags.StatisticsLeadingEdge
			)
		}

		if (hidesTrailing) {
			StatisticsEdgeFade(
				alignment = Alignment.CenterEnd,
				brush = Brush.horizontalGradient(colors = listOf(Color.Transparent, surface)),
				tag = SummaryUiTags.StatisticsTrailingEdge
			)
		}
	}
}
