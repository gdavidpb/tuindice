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
 * The assistant's monocle: a glass lens in a ring, with a chain and an amber sparkle. It marks only the header of the
 * tabs; inside the chat the assistant has no name and no icon.
 *
 * The icon paints its own colors, so it is drawn with `tint = Color.Unspecified`: a tint would repaint the glass, the
 * ring and the chain. The sparkle is the same amber in both themes.
 */
@Composable
fun rememberAssistantMonocleIcon(): ImageVector {
	val dark = TuIndiceDarkTheme.isDark()

	return remember(dark) {
		assistantMonocleIcon(dark = dark)
	}
}

// The design's geometry in its 26-unit viewport. Each circle is two half arcs, so the paths are SVG strings.
internal fun assistantMonocleIcon(dark: Boolean): ImageVector {
	val glass = Color(if (dark) GLASS_DARK else GLASS_LIGHT)
	val ink = Color(if (dark) INK_DARK else INK_LIGHT)
	val sparkle = Color(SPARKLE_AMBER)

	return ImageVector.Builder(
		name = "AssistantMonocle",
		defaultWidth = 24.dp,
		defaultHeight = 24.dp,
		viewportWidth = 26f,
		viewportHeight = 26f
	)
		.addPath(
			pathData = addPathNodes(GLASS_PATH),
			fill = SolidColor(glass)
		)
		.addPath(
			pathData = addPathNodes(RING_PATH),
			stroke = SolidColor(ink),
			strokeLineWidth = 2.6f
		)
		.addPath(
			pathData = addPathNodes(CHAIN_LARGE_PATH),
			fill = SolidColor(ink)
		)
		.addPath(
			pathData = addPathNodes(CHAIN_MIDDLE_PATH),
			fill = SolidColor(ink)
		)
		.addPath(
			pathData = addPathNodes(CHAIN_SMALL_PATH),
			fill = SolidColor(ink)
		)
		.addPath(
			pathData = addPathNodes(SPARKLE_PATH),
			fill = SolidColor(sparkle)
		)
		.build()
}

private const val GLASS_LIGHT = 0xFFF6F2E6L
private const val GLASS_DARK = 0xFF2A2A26L
private const val INK_LIGHT = 0xFF3A3A38L
private const val INK_DARK = 0xFFF4F1E8L
private const val SPARKLE_AMBER = 0xFFFABD00L

// Circles (cx, cy, r): glass (13, 10.5, 6.9), ring (13, 10.5, 8.2), chain links (5.2, 16.2, 1.45), (4.2, 19.3, 1.35)
// and (4.6, 22.3, 1.25). Each one starts at its rightmost point and comes back to it through its leftmost.
private const val GLASS_PATH = "M19.9 10.5A6.9 6.9 0 1 0 6.1 10.5A6.9 6.9 0 1 0 19.9 10.5Z"
private const val RING_PATH = "M21.2 10.5A8.2 8.2 0 1 0 4.8 10.5A8.2 8.2 0 1 0 21.2 10.5Z"
private const val CHAIN_LARGE_PATH = "M6.65 16.2A1.45 1.45 0 1 0 3.75 16.2A1.45 1.45 0 1 0 6.65 16.2Z"
private const val CHAIN_MIDDLE_PATH = "M5.55 19.3A1.35 1.35 0 1 0 2.85 19.3A1.35 1.35 0 1 0 5.55 19.3Z"
private const val CHAIN_SMALL_PATH = "M5.85 22.3A1.25 1.25 0 1 0 3.35 22.3A1.25 1.25 0 1 0 5.85 22.3Z"

// The sparkle of the design: two quadratic curves per point of the star.
private const val SPARKLE_PATH = "M13 5.9Q13.83 9.67 17.6 10.5Q13.83 11.33 13 15.1" +
	"Q12.17 11.33 8.4 10.5Q12.17 9.67 13 5.9Z"
