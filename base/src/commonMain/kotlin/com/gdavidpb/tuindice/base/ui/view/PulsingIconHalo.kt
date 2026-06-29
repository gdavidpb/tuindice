package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled

@Composable
fun PulsingIconHalo(
	modifier: Modifier = Modifier,
	color: Color,
	size: Dp = 28.dp,
	testTag: String? = null
) {
	val animationsEnabled = LocalTuIndiceAnimationsEnabled.current
	val transition = rememberInfiniteTransition(label = "PulsingIconHaloTransition")
	val scale = if (animationsEnabled) {
		transition.animateFloat(
			initialValue = PULSING_ICON_HALO_MIN_SCALE,
			targetValue = PULSING_ICON_HALO_MAX_SCALE,
			animationSpec = infiniteRepeatable(
				animation = tween(durationMillis = PULSING_ICON_HALO_DURATION_MILLIS),
				repeatMode = RepeatMode.Reverse
			),
			label = "PulsingIconHaloScale"
		).value
	} else {
		PULSING_ICON_HALO_MIN_SCALE
	}
	val alpha = if (animationsEnabled) {
		transition.animateFloat(
			initialValue = PULSING_ICON_HALO_MAX_ALPHA,
			targetValue = PULSING_ICON_HALO_MIN_ALPHA,
			animationSpec = infiniteRepeatable(
				animation = tween(durationMillis = PULSING_ICON_HALO_DURATION_MILLIS),
				repeatMode = RepeatMode.Reverse
			),
			label = "PulsingIconHaloAlpha"
		).value
	} else {
		PULSING_ICON_HALO_MAX_ALPHA
	}
	val testTagModifier = if (testTag == null) Modifier else Modifier.testTag(testTag)

	Box(
		modifier = modifier
			.size(size)
			.scale(scale)
			.alpha(alpha)
			.background(
				color = color,
				shape = CircleShape
			)
			.then(testTagModifier)
	)
}

private const val PULSING_ICON_HALO_DURATION_MILLIS = 900
private const val PULSING_ICON_HALO_MIN_SCALE = 0.82f
private const val PULSING_ICON_HALO_MAX_SCALE = 1.22f
private const val PULSING_ICON_HALO_MAX_ALPHA = 0.28f
private const val PULSING_ICON_HALO_MIN_ALPHA = 0.08f
