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
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.lerp
import com.gdavidpb.tuindice.base.ui.ReportComposition

internal const val SWEEP_MILLIS = 1600
private const val SWEEP_BASE_SOFTENING = 0.12f
private const val SWEEP_WIDTH_FRACTION = 0.65f
private const val SWEEP_HIGHLIGHT_START = 0.35f
private const val SWEEP_HIGHLIGHT_END = 0.65f

@Composable
internal fun SweepingLabel(
	text: String,
	modifier: Modifier
) {
	ReportComposition("sweeping-label")

	val sweep = rememberInfiniteTransition(label = "AssistantLabelSweep").animateFloat(
		initialValue = 0f,
		targetValue = 1f,
		animationSpec = infiniteRepeatable(
			animation = tween(durationMillis = SWEEP_MILLIS, easing = LinearEasing),
			repeatMode = RepeatMode.Restart
		),
		label = "AssistantLabelSweepProgress"
	)
	val baseColor = lerp(
		MaterialTheme.colorScheme.onSurfaceVariant,
		MaterialTheme.colorScheme.background,
		SWEEP_BASE_SOFTENING
	)
	val highlightColor = MaterialTheme.colorScheme.onSurface
	// The same brush while the colors hold. The text asks it for a shader when it draws, and the shader reads the sweep
	// then: each frame of the sweep redraws the label and composes none of it.
	val brush = remember(sweep, baseColor, highlightColor) { SweepingLabelBrush(sweep, baseColor, highlightColor) }

	Text(
		text = text,
		modifier = modifier,
		style = MaterialTheme.typography.bodyMedium.copy(brush = brush)
	)
}

// A concentrated band of light travels from one edge of the label to the other.
private class SweepingLabelBrush(
	private val sweep: State<Float>,
	private val baseColor: Color,
	private val highlightColor: Color
) : ShaderBrush() {
	override fun createShader(size: Size): Shader {
		val width = size.width
		val bandWidth = width * SWEEP_WIDTH_FRACTION
		val start = (width + bandWidth) * sweep.value - bandWidth
		return LinearGradientShader(
			from = Offset(x = start, y = 0f),
			to = Offset(x = start + bandWidth, y = 0f),
			colors = listOf(baseColor, highlightColor, highlightColor, baseColor),
			colorStops = listOf(0f, SWEEP_HIGHLIGHT_START, SWEEP_HIGHLIGHT_END, 1f)
		)
	}
}
