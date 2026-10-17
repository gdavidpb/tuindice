package com.gdavidpb.tuindice.base.ui.icon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceDarkTheme

/**
 * The assistant's sparkle: a four-point star in amber. It marks only the header of the tabs and the «Preguntar»
 * actions; inside the chat the assistant has no name and no icon.
 *
 * The icon paints its own color, so it is drawn with `tint = Color.Unspecified`. The amber follows the theme with the
 * same tones as the loading animation's glyph: dark amber on light surfaces, bright amber on dark ones.
 */
@Composable
fun rememberAssistantSparkleIcon(): ImageVector {
	val dark = TuIndiceDarkTheme.isDark()

	return remember(dark) {
		assistantSparkleIcon(dark = dark)
	}
}

internal fun assistantSparkleIcon(dark: Boolean): ImageVector {
	val sparkle = Color(if (dark) SPARKLE_DARK else SPARKLE_LIGHT)

	return ImageVector.Builder(
		name = "AssistantSparkle",
		defaultWidth = 24.dp,
		defaultHeight = 24.dp,
		viewportWidth = 24f,
		viewportHeight = 24f
	)
		.addPath(
			pathData = addPathNodes(SPARKLE_PATH),
			fill = SolidColor(sparkle)
		)
		.build()
}

private const val SPARKLE_LIGHT = 0xFF785900L
private const val SPARKLE_DARK = 0xFFFABD00L

// The design's sparkle centred in the 24-unit viewport with a radius of 10: two quadratic curves per point of the star,
// their control points 1.8 units off the centre lines.
private const val SPARKLE_PATH = "M12 2Q13.8 10.2 22 12Q13.8 13.8 12 22Q10.2 13.8 2 12Q10.2 10.2 12 2Z"
