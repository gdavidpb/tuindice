package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.model.TuIndiceProgressSegment
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius

/**
 * The bar of one share of a whole, the one every progress bar of the app is: a track of
 * [TuIndiceComponentSizes.ProgressBarHeight] in `outlineVariant`, with rounded ends. [fraction] of it is filled in
 * [color], from its start; the share is held between nothing and all of it. [fillTag] names the fill for a test.
 */
@Composable
fun TuIndiceProgressBar(
	fraction: Float,
	color: Color,
	modifier: Modifier = Modifier,
	fillTag: String? = null
) {
	TuIndiceProgressBar(
		segments = listOf(TuIndiceProgressSegment(fraction = fraction, color = color, tag = fillTag)),
		modifier = modifier
	)
}

/**
 * The bar of a whole split into [segments]: each one as wide as its share and drawn in its own colour, one after the
 * other from the start, on the same track and at the same height as the single-colour bar. The shares are held
 * between nothing and all of the track, and a segment's [TuIndiceProgressSegment.tag] names its drawn piece for a test.
 *
 * Without a [gap] the segments lie against each other: the track shows through as the unfilled part of the bar, and
 * the whole bar has rounded ends. With a [gap] they are separate pills, each rounded and with that space between them,
 * and the track is not drawn, since the space would show it instead of the surface behind.
 *
 * [progress] is how much of the bar is drawn, from nothing to all of it: a chart that grows its bar passes it from 0
 * up to 1, and each segment keeps its place as the bar grows.
 */
@Composable
fun TuIndiceProgressBar(
	segments: List<TuIndiceProgressSegment>,
	modifier: Modifier = Modifier,
	gap: Dp = 0.dp,
	progress: Float = 1f
) {
	val shape = RoundedCornerShape(TuIndiceRadius.Full)
	val pills = gap > 0.dp
	val track = MaterialTheme.colorScheme.outlineVariant
	val frame = modifier
		.fillMaxWidth()
		.height(TuIndiceComponentSizes.ProgressBarHeight)

	BoxWithConstraints(modifier = if (pills) frame else frame.clip(shape).background(track)) {
		val gapDp = gap.value
		val available = maxWidth.value - gapDp * (segments.size - 1).coerceAtLeast(0)
		var start = 0f

		segments.forEach { segment ->
			val share = segment.fraction.coerceIn(0f, 1f) * available
			val piece = Modifier
				.align(Alignment.CenterStart)
				.offset(x = (start * progress).dp)
				.width((share * progress).dp)
				.fillMaxHeight()

			Box(
				modifier = (if (pills) piece.clip(shape) else piece)
					.background(segment.color)
					.then(segment.tag?.let { tag -> Modifier.testTag(tag) } ?: Modifier)
			)

			start += share + gapDp
		}
	}
}
