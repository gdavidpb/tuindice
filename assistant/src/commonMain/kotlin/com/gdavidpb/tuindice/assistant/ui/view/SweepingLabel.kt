package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onSizeChanged

@Composable
internal fun SweepingLabel(
	text: String,
	modifier: Modifier
) {
	val width = remember { mutableStateOf(0f) }
	val sweep = rememberInfiniteTransition(label = "AssistantLabelSweep").animateFloat(
		initialValue = 0f,
		targetValue = 1f,
		animationSpec = infiniteRepeatable(
			animation = tween(durationMillis = SWEEP_MILLIS, easing = LinearEasing),
			repeatMode = RepeatMode.Restart
		),
		label = "AssistantLabelSweepProgress"
	)
	val baseColor = MaterialTheme.colorScheme.onSurfaceVariant
	val lightColor = MaterialTheme.colorScheme.onSurface
	val shift = width.value * SWEEP_TRAVEL * sweep.value
	val brush = Brush.linearGradient(
		0f to baseColor,
		SWEEP_HIGHLIGHT_STOP to lightColor,
		1f to baseColor,
		start = Offset(x = shift - width.value, y = 0f),
		end = Offset(x = shift, y = 0f)
	)

	Text(
		text = text,
		modifier = modifier.onSizeChanged { size -> width.value = size.width.toFloat() },
		style = MaterialTheme.typography.bodyMedium.copy(brush = brush)
	)
}
