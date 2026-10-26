package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.LocalTuIndiceCompositionProbe
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// The probe names the halo when it composes. The pulse runs for as long as the halo shows: its frames redraw the halo
// in the layer, and compose none of it.
@OptIn(ExperimentalTestApi::class)
class PulsingIconHaloCompositionUiTest {
	@Test
	fun when_haloPulses_then_eachFrameRedrawsTheHaloWithoutRecomposingIt() = runTuIndiceUiTest {
		val compositions = mutableListOf<String>()
		val probe: (String) -> Unit = { name -> compositions += name }
		mainClock.autoAdvance = false

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalTuIndiceCompositionProbe provides probe,
				LocalTuIndiceAnimationsEnabled provides true
			) {
				PulsingIconHalo(color = Color.Red)
			}
		}

		// The first composition and its first frame come before the pulse: they are not part of it.
		waitForIdle()
		mainClock.advanceTimeByFrame()
		waitForIdle()
		val started = compositions.count { it == "pulsing-halo" }
		assertTrue(started > 0, "the halo composes")

		repeat(30) { mainClock.advanceTimeByFrame() }
		waitForIdle()

		assertEquals(
			expected = started,
			actual = compositions.count { it == "pulsing-halo" },
			message = "the frames of the pulse compose no halo"
		)
	}

	// With animations off the halo composes once, and paints the fixed scale and alpha it has always had.
	@Test
	fun when_animationsAreOff_then_haloComposesOnceAndPaintsTheFixedScaleAndAlpha() = runTuIndiceUiTest {
		val compositions = mutableListOf<String>()
		val probe: (String) -> Unit = { name -> compositions += name }
		mainClock.autoAdvance = false

		setTuIndiceTestContent(density = 1f) {
			CompositionLocalProvider(
				LocalTuIndiceCompositionProbe provides probe,
				LocalTuIndiceAnimationsEnabled provides false
			) {
				Column(verticalArrangement = Arrangement.spacedBy(40.dp)) {
					PulsingIconHalo(color = Color.Red, testTag = HaloTag)
					// The fixed scale and alpha of the halo with animations off, painted the way Modifier.scale and
					// Modifier.alpha paint them.
					Box(
						modifier = Modifier
							.size(28.dp)
							.scale(FixedScale)
							.alpha(FixedAlpha)
							.background(color = Color.Red, shape = CircleShape)
							.testTag(ReferenceTag)
					)
				}
			}
		}

		// The first composition and its first frame come before anything else: the halo composes once.
		waitForIdle()
		mainClock.advanceTimeByFrame()
		waitForIdle()
		assertEquals(1, compositions.count { it == "pulsing-halo" }, "the halo composes once")

		repeat(30) { mainClock.advanceTimeByFrame() }
		waitForIdle()
		assertEquals(1, compositions.count { it == "pulsing-halo" }, "the halo composes no more with animations off")

		val halo = onNodeWithTag(HaloTag).captureToImage().toPixelMap()
		val reference = onNodeWithTag(ReferenceTag).captureToImage().toPixelMap()
		assertTrue(inkPixels(halo) > 0, "the halo paints")
		assertEquals(0, differingPixels(halo, reference), "the halo paints the fixed scale and alpha")
	}

	// Pixels that differ between two pictures; a picture of another size differs in all of its pixels.
	private fun differingPixels(first: PixelMap, second: PixelMap): Int {
		if (first.width != second.width || first.height != second.height) return first.width * first.height
		var count = 0
		for (x in 0 until first.width) {
			for (y in 0 until first.height) {
				if (first[x, y] != second[x, y]) count++
			}
		}
		return count
	}

	// Pixels that differ from the corner, so a blank picture counts none.
	private fun inkPixels(picture: PixelMap): Int {
		val background = picture[0, 0]
		var count = 0
		for (x in 0 until picture.width) {
			for (y in 0 until picture.height) {
				if (picture[x, y] != background) count++
			}
		}
		return count
	}

	private companion object {
		const val HaloTag = "pulsing_icon_halo"
		const val ReferenceTag = "fixed_halo_reference"
		const val FixedScale = 0.82f
		const val FixedAlpha = 0.28f
	}
}
