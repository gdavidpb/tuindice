package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// The touch area of each drawn segment, from the final layout, so a tap during the growth lands where the segment
// will be. The areas cover the bar in order without overlap: a narrow segment takes the room it lacks from the wider
// segments beside it, so no segment is hidden under another. An empty segment has no area.
@Composable
fun BoxScope.StatisticsStackedTargets(
	segments: List<StatisticsSegment>,
	spans: List<StatisticsChartGeometry.Span>,
	labels: StatisticsChartLabels,
	openIndex: MutableState<Int?>
) {
	// The spans of a bar with something drawn share its whole width.
	val barWidth = spans.fold(0f) { width, span -> width + span.widthDp }
	val drawn = StatisticsChartGeometry.drawnSegments(segments.map { segment -> segment.value })
	val touches = StatisticsChartGeometry.touchSpans(
		widthsDp = drawn.map { (index, _) -> spans[index].widthDp },
		totalDp = barWidth
	)

	drawn.forEachIndexed { position, (index, _) ->
		key(index) {
			val segment = segments[index]
			val touch = touches[position]

			Box(
				modifier = Modifier
					.align(Alignment.CenterStart)
					.offset(x = touch.startDp.dp)
					// As tall as a finger, centred on the thinner bar and reaching past it above and below.
					.requiredSize(width = touch.widthDp.dp, height = StatisticsChartDefaults.TargetMinSize)
			) {
				StatisticsTooltipTarget(
					target = labels.targetOf(index, segment.label, segment.tooltip),
					isOpen = openIndex.value == index,
					onOpenChange = { open -> openIndex.value = if (open) index else null }
				) {}
			}
		}
	}
}
