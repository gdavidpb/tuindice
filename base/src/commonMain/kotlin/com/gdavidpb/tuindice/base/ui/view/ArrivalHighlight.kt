package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import kotlinx.coroutines.delay

/**
 * Marks the composable as the one a jump landed on: a border in the primary colour, drawn on [shape],
 * that fades in, holds, and fades out. Each change of [trigger] to a non-null value plays it once; a
 * null trigger draws nothing. [onFinished] runs when it ends, so the caller can stop asking for it.
 *
 * With animations disabled nothing is drawn and [onFinished] runs at once. The border sits inside the
 * bounds of the composable, so the layout around it does not move while it plays.
 *
 * It is drawn within the bounds it is given, so apply it last in the container's modifier chain, after
 * the container's margins and padding: a margin applied before it is framed by the border too.
 */
@Composable
fun Modifier.arrivalHighlight(
	trigger: Any?,
	shape: Shape,
	onFinished: () -> Unit
): Modifier {
	val animationsEnabled = LocalTuIndiceAnimationsEnabled.current
	val currentOnFinished by rememberUpdatedState(onFinished)
	val alpha = remember { Animatable(0f) }
	val borderColor = MaterialTheme.colorScheme.primary

	LaunchedEffect(trigger, animationsEnabled) {
		alpha.snapTo(0f)

		if (trigger == null) return@LaunchedEffect

		if (!animationsEnabled) {
			currentOnFinished()
			return@LaunchedEffect
		}

		alpha.animateTo(targetValue = 1f, animationSpec = tween(durationMillis = ARRIVAL_HIGHLIGHT_FADE_IN_MILLIS))
		delay(ARRIVAL_HIGHLIGHT_HOLD_MILLIS.toLong())
		alpha.animateTo(targetValue = 0f, animationSpec = tween(durationMillis = ARRIVAL_HIGHLIGHT_FADE_OUT_MILLIS))
		currentOnFinished()
	}

	return drawWithContent {
		drawContent()

		val currentAlpha = alpha.value
		if (currentAlpha > 0f) {
			val strokeWidth = ArrivalHighlightBorderWidth.toPx()
			val inset = strokeWidth / 2f
			val outline = shape.createOutline(
				size = Size(width = size.width - strokeWidth, height = size.height - strokeWidth),
				layoutDirection = layoutDirection,
				density = this
			)

			translate(left = inset, top = inset) {
				drawPath(
					path = pathOf(outline),
					color = borderColor,
					alpha = currentAlpha,
					style = Stroke(width = strokeWidth)
				)
			}
		}
	}
}

private fun pathOf(outline: Outline): Path {
	val path = Path()

	when (outline) {
		is Outline.Rectangle -> path.addRect(outline.rect)
		is Outline.Rounded -> path.addRoundRect(outline.roundRect)
		is Outline.Generic -> path.addPath(outline.path)
	}

	return path
}

private const val ARRIVAL_HIGHLIGHT_FADE_IN_MILLIS = 250
private const val ARRIVAL_HIGHLIGHT_HOLD_MILLIS = 700
private const val ARRIVAL_HIGHLIGHT_FADE_OUT_MILLIS = 600
private val ArrivalHighlightBorderWidth = 2.dp
