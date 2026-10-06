package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.testing.PensumPixelHost
import com.gdavidpb.tuindice.pensum.testing.assertPixelColor
import com.gdavidpb.tuindice.pensum.testing.pensumHostPixel
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PensumProgressRingUiTest {
	@Test
	fun when_ringIsRendered_then_occupiesItsTwentyFourDpSlot() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box(modifier = Modifier.testTag(RING_TAG)) {
				PensumProgressRing(progress = 0.4f)
			}
		}

		onNodeWithTag(RING_TAG)
			.assertWidthIsEqualTo(24.dp)
			.assertHeightIsEqualTo(24.dp)
	}

	@Test
	fun when_progressIsHalf_then_onlyTheClockwiseHalfIsDrawnAsApproved() = runTuIndiceUiTest {
		val ringColors = setRingContent(progress = 0.5f)

		assertPixelColor(expected = ringColors.approved, actual = pensumHostPixel(RING_FAR, 0.5f))
		assertPixelColor(expected = ringColors.track, actual = pensumHostPixel(RING_NEAR, 0.5f))
	}

	@Test
	fun when_progressExceedsOne_then_wholeRingIsApproved() = runTuIndiceUiTest {
		val ringColors = setRingContent(progress = 1.7f)

		assertPixelColor(expected = ringColors.approved, actual = pensumHostPixel(RING_FAR, 0.5f))
		assertPixelColor(expected = ringColors.approved, actual = pensumHostPixel(RING_NEAR, 0.5f))
		assertPixelColor(expected = ringColors.approved, actual = pensumHostPixel(0.5f, RING_FAR))
	}

	@Test
	fun when_progressIsNegative_then_onlyTheTrackIsDrawn() = runTuIndiceUiTest {
		val ringColors = setRingContent(progress = -0.5f)

		assertPixelColor(expected = ringColors.track, actual = pensumHostPixel(RING_FAR, 0.5f))
		assertPixelColor(expected = ringColors.track, actual = pensumHostPixel(RING_NEAR, 0.5f))
		assertPixelColor(expected = ringColors.track, actual = pensumHostPixel(0.5f, RING_FAR))
	}
}

private class RingColors(
	val approved: Color,
	val track: Color
)

@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.setRingContent(progress: Float): RingColors {
	var ringColors: RingColors? = null

	setTuIndiceTestContent {
		PensumPixelHost {
			val graphColors = pensumGraphColors()
			ringColors = RingColors(approved = graphColors.approved, track = graphColors.panelBorder)
			PensumProgressRing(progress = progress)
		}
	}
	waitForIdle()

	return checkNotNull(ringColors)
}

private const val RING_TAG = "pensum_progress_ring_host"

// The 4dp stroke is centred on the slot edge, so its inner half covers the first
// and last twelfth of each axis. The arc starts at twelve o'clock and runs clockwise.
private const val RING_NEAR = 0.04f
private const val RING_FAR = 0.96f
