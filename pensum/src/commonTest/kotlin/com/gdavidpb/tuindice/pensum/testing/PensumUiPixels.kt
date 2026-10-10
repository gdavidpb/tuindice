package com.gdavidpb.tuindice.pensum.testing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import com.gdavidpb.tuindice.base.ui.style.TuIndiceDarkTheme
import kotlin.math.abs
import kotlin.test.assertTrue

const val PENSUM_PIXEL_HOST_TAG = "pensum_pixel_host"

// Drawing-only composables expose no semantics, so their tests read pixels. Three
// pixels per dp keeps every sampled point well inside a stroke instead of on its
// antialiased edge.
private const val PIXEL_HOST_DENSITY = 3f
private const val COLOR_TOLERANCE = 0.05f

/**
 * Hosts [content] on an opaque white, light-themed box tagged
 * [PENSUM_PIXEL_HOST_TAG], so a capture of the host has a known background and
 * the palette does not depend on the simulator's appearance.
 */
@Composable
fun PensumPixelHost(
	isDark: Boolean = false,
	content: @Composable () -> Unit
) {
	CompositionLocalProvider(
		LocalDensity provides Density(PIXEL_HOST_DENSITY),
		TuIndiceDarkTheme.Local provides isDark
	) {
		Box(
			modifier = Modifier
				.testTag(PENSUM_PIXEL_HOST_TAG)
				.background(Color.White)
		) {
			content()
		}
	}
}

/** Reads the host pixel at a position given as fractions of its width and height. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.pensumHostPixel(xFraction: Float, yFraction: Float): Color {
	waitForIdle()

	val pixels = onNodeWithTag(PENSUM_PIXEL_HOST_TAG).captureToImage().toPixelMap()
	val x = (pixels.width * xFraction).toInt().coerceIn(0, pixels.width - 1)
	val y = (pixels.height * yFraction).toInt().coerceIn(0, pixels.height - 1)

	return pixels[x, y]
}

fun assertPixelColor(expected: Color, actual: Color) {
	assertTrue(
		actual = expected.isCloseTo(actual),
		message = "Expected a pixel close to $expected but was $actual."
	)
}

fun assertPixelColorIsNot(unexpected: Color, actual: Color) {
	assertTrue(
		actual = !unexpected.isCloseTo(actual),
		message = "Expected a pixel different from $unexpected but was $actual."
	)
}

private fun Color.isCloseTo(other: Color): Boolean {
	return abs(red - other.red) <= COLOR_TOLERANCE &&
		abs(green - other.green) <= COLOR_TOLERANCE &&
		abs(blue - other.blue) <= COLOR_TOLERANCE &&
		abs(alpha - other.alpha) <= COLOR_TOLERANCE
}
