package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.testing.PensumPixelHost
import com.gdavidpb.tuindice.pensum.testing.assertPixelColor
import com.gdavidpb.tuindice.pensum.testing.pensumHostPixel
import com.gdavidpb.tuindice.pensum.ui.model.PensumCurrentRouteIcon
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PensumStatusIconMarkerUiTest {
	@Test
	fun when_markerSizeIsProvided_then_markerOccupiesExactlyThatSize() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumStatusIconMarker(
				imageVector = Icons.Filled.Check,
				tint = TINT,
				hasBuiltInContainer = false,
				markerSize = 32.dp,
				iconSize = 16.dp,
				borderWidth = 2.dp,
				modifier = Modifier.testTag(MARKER_TAG)
			)
		}

		onNodeWithTag(MARKER_TAG)
			.assertWidthIsEqualTo(32.dp)
			.assertHeightIsEqualTo(32.dp)
	}

	@Test
	fun when_iconHasNoBuiltInContainer_then_drawsTintedRingOverTheGivenBackground() =
		runTuIndiceUiTest {
			setTuIndiceTestContent {
				PensumPixelHost {
					PensumStatusIconMarker(
						imageVector = Icons.Filled.Check,
						tint = TINT,
						hasBuiltInContainer = false,
						markerSize = 20.dp,
						iconSize = 12.dp,
						borderWidth = 2.dp,
						backgroundColor = BACKGROUND
					)
				}
			}

			assertPixelColor(expected = TINT, actual = pensumHostPixel(0.5f, RING_EDGE))
			assertPixelColor(expected = BACKGROUND, actual = pensumHostPixel(0.5f, INSIDE_RING))
		}

	@Test
	fun when_iconHasBuiltInContainer_then_iconFillsTheMarkerAndIgnoresTheBackground() =
		runTuIndiceUiTest {
			setTuIndiceTestContent {
				PensumPixelHost {
					PensumStatusIconMarker(
						imageVector = PensumCurrentRouteIcon,
						tint = TINT,
						hasBuiltInContainer = true,
						markerSize = 20.dp,
						iconSize = 12.dp,
						borderWidth = 2.dp,
						backgroundColor = BACKGROUND
					)
				}
			}

			assertPixelColor(expected = Color.White, actual = pensumHostPixel(0.5f, RING_EDGE))
			assertPixelColor(expected = TINT, actual = pensumHostPixel(0.5f, INSIDE_RING))
			assertPixelColor(expected = Color.White, actual = pensumHostPixel(0.5f, BETWEEN_RING_AND_DOT))
			assertPixelColor(expected = TINT, actual = pensumHostPixel(0.5f, 0.5f))
		}
}

private const val MARKER_TAG = "pensum_status_icon_marker"

// Fractions of the 20dp marker along its vertical axis: inside the 2dp border, in
// the gap between that border and the 12dp glyph, and between the built-in glyph's
// ring and its centre dot.
private const val RING_EDGE = 0.04f
private const val INSIDE_RING = 0.13f
private const val BETWEEN_RING_AND_DOT = 0.24f

private val TINT = Color(0xFF2E7D32)
private val BACKGROUND = Color(0xFFFF00FF)
