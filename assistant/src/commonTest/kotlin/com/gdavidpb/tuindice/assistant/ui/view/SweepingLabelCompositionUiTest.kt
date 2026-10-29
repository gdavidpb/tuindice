package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.base.ui.LocalTuIndiceCompositionProbe
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// The probe names the label when it composes. The light sweeps across the label for as long as a turn runs: its frames
// redraw the label over the text, and compose none of it.
@OptIn(ExperimentalTestApi::class)
class SweepingLabelCompositionUiTest {
	@Test
	fun when_labelSweeps_then_eachFrameRedrawsTheLabelWithoutRecomposingIt() = runTuIndiceUiTest {
		val compositions = mutableListOf<String>()
		val probe: (String) -> Unit = { name -> compositions += name }
		mainClock.autoAdvance = false

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalTuIndiceCompositionProbe provides probe,
				LocalTuIndiceAnimationsEnabled provides true
			) {
				SweepingLabel(text = "Consultando", modifier = Modifier)
			}
		}

		// The first composition and its first frame come before the sweep: they are not part of it.
		waitForIdle()
		mainClock.advanceTimeByFrame()
		waitForIdle()
		val started = compositions.count { it == "sweeping-label" }
		assertTrue(started > 0, "the label composes")

		repeat(30) { mainClock.advanceTimeByFrame() }
		waitForIdle()

		assertEquals(
			expected = started,
			actual = compositions.count { it == "sweeping-label" },
			message = "the frames of the sweep compose no label"
		)
	}

	// Across the sweep the label keeps its text, and stays on screen.
	@Test
	fun when_theSweepRuns_then_theLabelKeepsItsTextAndStaysDisplayed() = runTuIndiceUiTest {
		mainClock.autoAdvance = false

		setTuIndiceTestContent {
			SweepingLabel(text = LabelText, modifier = Modifier.testTag(LabelTag))
		}

		waitForIdle()
		repeat(SweepChecks) {
			repeat(SweepCheckFrames) { mainClock.advanceTimeByFrame() }
			waitForIdle()
			onNodeWithTag(LabelTag).assertTextEquals(LabelText).assertIsDisplayed()
		}
	}

	// The sweep moves its highlight across the label: a quarter of the way along, the label is drawn differently.
	// A sweep that had stopped would draw the same pixels, and this test would fail.
	@Test
	fun when_theSweepAdvances_then_itsHighlightMovesAcrossTheLabel() = runTuIndiceUiTest {
		mainClock.autoAdvance = false
		setTuIndiceTestContent {
			SweepingLabel(text = LabelText, modifier = Modifier.testTag(LabelTag))
		}

		waitForIdle()
		val first = onNodeWithTag(LabelTag).captureToImage().toPixelMap()

		mainClock.advanceTimeBy(SWEEP_MILLIS.toLong() / 4)
		waitForIdle()
		val later = onNodeWithTag(LabelTag).captureToImage().toPixelMap()

		val changed = changedPixels(first, later)
		assertTrue(changed >= MinChangedPixels, "the highlight moves across the label: $changed pixels changed")
	}

	private fun changedPixels(first: PixelMap, later: PixelMap): Int {
		var count = 0

		for (x in 0 until minOf(first.width, later.width)) {
			for (y in 0 until minOf(first.height, later.height)) {
				if (first[x, y].differsFrom(later[x, y])) count++
			}
		}
		return count
	}

	private fun Color.differsFrom(other: Color): Boolean =
		abs(red - other.red) > ChangeTolerance ||
			abs(green - other.green) > ChangeTolerance ||
			abs(blue - other.blue) > ChangeTolerance

	private companion object {
		const val LabelTag = "sweeping_label"
		const val LabelText = "Consultando horario"
		const val SweepChecks = 8
		const val SweepCheckFrames = 10
		const val ChangeTolerance = 0.02f
		const val MinChangedPixels = 10
	}
}
