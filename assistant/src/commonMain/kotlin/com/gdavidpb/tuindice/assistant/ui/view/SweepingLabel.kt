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
import com.gdavidpb.tuindice.base.ui.ReportComposition

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
	val baseColor = MaterialTheme.colorScheme.onSurfaceVariant
	val lightColor = MaterialTheme.colorScheme.onSurface
	// The same brush while the colors hold. The text asks it for a shader when it draws, and the shader reads the sweep
	// then: each frame of the sweep redraws the label and composes none of it.
	val brush = remember(baseColor, lightColor) { SweepingLabelBrush(sweep, baseColor, lightColor) }

	Text(
		text = text,
		modifier = modifier,
		style = MaterialTheme.typography.bodyMedium.copy(brush = brush)
	)
}

// The highlight of the sweep, with the base color on both sides of it. Its shader is made with the sweep of the frame
// being drawn. The first frame's edge pixels differ from the version this replaces; the later frames paint the same
// pixels as that version.
private class SweepingLabelBrush(
	private val sweep: State<Float>,
	private val baseColor: Color,
	private val lightColor: Color
) : ShaderBrush() {
	override fun createShader(size: Size): Shader {
		val width = size.width
		val shift = width * SWEEP_TRAVEL * sweep.value
		return LinearGradientShader(
			from = Offset(x = shift - width, y = 0f),
			to = Offset(x = shift, y = 0f),
			colors = listOf(baseColor, lightColor, baseColor),
			colorStops = listOf(0f, SWEEP_HIGHLIGHT_STOP, 1f)
		)
	}
}
