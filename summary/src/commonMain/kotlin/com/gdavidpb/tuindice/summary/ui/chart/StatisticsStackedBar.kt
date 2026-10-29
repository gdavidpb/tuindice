package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.gdavidpb.tuindice.base.ui.ReportComposition
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
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
	ReportComposition("statistics-stacked")

	val openIndex = remember { mutableStateOf<Int?>(null) }
	val animationsEnabled = LocalTuIndiceAnimationsEnabled.current
	val growth = remember { Animatable(0f) }

	LaunchedEffect(animationsEnabled) {
		if (animationsEnabled) {
			growth.animateTo(1f, tween(durationMillis = StatisticsChartDefaults.GrowMillis))
		}
	}

	// The growth is read by the track where it is laid out, so a frame of it composes neither the bar nor its targets.
	val progress: () -> Float = { if (animationsEnabled) growth.value else 1f }

	BoxWithConstraints(
		modifier = modifier
			.fillMaxWidth()
			// The bar takes only its own height: the targets reach past it, so a finger can hit them without the
			// bar leaving room around itself.
			.height(TuIndiceComponentSizes.ProgressBarHeight)
			.testTag(SummaryUiTags.statisticsChart(labels.key))
			.semantics { contentDescription = labels.summary }
	) {
		val spans = remember(segments, maxWidth) {
			StatisticsStackGeometry.segmentSpans(segments.map { segment -> segment.value }, maxWidth.value)
		}

		StatisticsStackedTrack(
			segments = segments,
			progress = progress,
			labels = labels,
			openIndex = openIndex
		)

		StatisticsStackedTargets(
			segments = segments,
			spans = spans,
			labels = labels,
			openIndex = openIndex
		)
	}
}
