package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.model.TuIndiceProgressSegment
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.view.TuIndiceProgressBar
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

/**
 * One horizontal bar split into [segments], each as wide as its share of the whole and drawn in its own colour.
 * The bar is drawn at the height of the app's progress bars and grows from the left to its full width unless the
 * animations are off. Tapping a segment opens its tooltip and closes the one that was open. A segment with no value
 * is not drawn and has no target. A drawn segment has a target at least as wide as a finger wherever the bar is wide
 * enough to give each one that, and as tall as a finger around the thinner bar, reaching past it without taking room
 * in the layout; the targets never overlap, so each drawn segment can be tapped.
 */
@Composable
fun StatisticsStackedBar(
	segments: List<StatisticsSegment>,
	labels: StatisticsChartLabels,
	modifier: Modifier = Modifier
) {
	val openIndex = remember { mutableStateOf<Int?>(null) }
	val animationsEnabled = LocalTuIndiceAnimationsEnabled.current
	val growth = remember { Animatable(0f) }

	LaunchedEffect(animationsEnabled) {
		if (animationsEnabled) {
			growth.animateTo(1f, tween(durationMillis = StatisticsChartDefaults.GrowMillis))
		}
	}

	val progress = if (animationsEnabled) growth.value else 1f

	BoxWithConstraints(
		modifier = modifier
			.fillMaxWidth()
			// The bar takes only its own height: the targets reach past it, so a finger can hit them without the
			// bar leaving room around itself.
			.height(TuIndiceComponentSizes.ProgressBarHeight)
			.testTag(SummaryUiTags.statisticsChart(labels.key))
			.semantics { contentDescription = labels.summary }
	) {
		val spans = StatisticsChartGeometry.segmentSpans(segments.map { segment -> segment.value }, maxWidth.value)

		StackedTrack(
			segments = segments,
			progress = progress,
			labels = labels,
			openIndex = openIndex
		)

		StackedTargets(
			segments = segments,
			spans = spans,
			labels = labels,
			openIndex = openIndex
		)
	}
}

// The coloured segments, drawn by the shared progress bar at its height and with its rounded ends. Each one is dimmed
// while another is open, and each keeps the tag of its index.
@Composable
private fun BoxScope.StackedTrack(
	segments: List<StatisticsSegment>,
	progress: Float,
	labels: StatisticsChartLabels,
	openIndex: MutableState<Int?>
) {
	val hasSelection = openIndex.value != null
	val drawn = segments.withIndex().filter { (_, segment) -> segment.value > 0f }
	val total = drawn.sumOf { (_, segment) -> segment.value.toDouble() }.toFloat()

	TuIndiceProgressBar(
		segments = drawn.map { (index, segment) ->
			TuIndiceProgressSegment(
				fraction = segment.value / total,
				color = segment.color.copy(
					alpha = StatisticsChartDefaults.alphaOf(openIndex.value == index, hasSelection)
				),
				tag = SummaryUiTags.statisticsFill(labels.key, index)
			)
		},
		modifier = Modifier.align(Alignment.Center),
		progress = progress
	)
}

// The touch area of each drawn segment, from the final layout, so a tap during the growth lands where the segment
// will be. The areas cover the bar in order without overlap: a narrow segment takes the room it lacks from the wider
// segments beside it, so no segment is hidden under another. An empty segment has no area.
@Composable
private fun BoxScope.StackedTargets(
	segments: List<StatisticsSegment>,
	spans: List<StatisticsChartGeometry.Span>,
	labels: StatisticsChartLabels,
	openIndex: MutableState<Int?>
) {
	// The spans of a bar with something drawn share its whole width.
	val barWidth = spans.fold(0f) { width, span -> width + span.widthDp }
	val drawn = segments.indices.filter { index -> segments[index].value > 0f }
	val touches = StatisticsChartGeometry.touchSpans(
		widthsDp = drawn.map { index -> spans[index].widthDp },
		totalDp = barWidth
	)

	drawn.forEachIndexed { position, index ->
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
