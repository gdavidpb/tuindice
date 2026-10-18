package com.gdavidpb.tuindice.base.ui.icon

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * The assistant's sparkle: a four-point star. It marks the top bar, the «Preguntar» actions (the swipe action of
 * Evaluations and the detail sheet of Pensum) and the assistant's items of About.
 *
 * Like a Material icon it has no color of its own: it is filled in black, and every surface tints it with
 * [assistantSparkleTint], the accent of the app in the light and the dark theme.
 */
@Composable
fun rememberAssistantSparkleIcon(): ImageVector {
	return remember { assistantSparkleIcon() }
}

/**
 * The color of the sparkle: the app's accent, [MaterialTheme]'s primary, in both themes. None of the sparkle's surfaces
 * is filled with the primary (the swipe action sits on surfaceVariant), so no surface needs onPrimary instead.
 */
@Composable
fun assistantSparkleTint(): Color = MaterialTheme.colorScheme.primary

internal fun assistantSparkleIcon(): ImageVector {
	return ImageVector.Builder(
		name = "AssistantSparkle",
		defaultWidth = 24.dp,
		defaultHeight = 24.dp,
		viewportWidth = 24f,
		viewportHeight = 24f
	)
		.addPath(
			pathData = addPathNodes(SPARKLE_PATH),
			fill = SolidColor(Color.Black)
		)
		.build()
}

// The design's sparkle centred in the 24-unit viewport with a radius of 10: two quadratic curves per point of the star,
// their control points 1.8 units off the centre lines.
private const val SPARKLE_PATH = "M12 2Q13.8 10.2 22 12Q13.8 13.8 12 22Q10.2 13.8 2 12Q10.2 10.2 12 2Z"
