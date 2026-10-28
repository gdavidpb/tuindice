package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.ui.style.AssistantChatMotion
import com.gdavidpb.tuindice.base.ui.LocalTuIndiceCompositionProbe
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.testkit.ui.advanceAnimationsBy
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AssistantRevealedTextViewUiTest {
	// A behavior test of the view, not a regression test: the whole text is in the node from the first frame.
	@Test
	fun when_textIsRevealing_then_theWholeTextIsThereFromTheFirstFrame() = runTuIndiceUiTest {
		// The reveal runs on the test clock: its frames go by only when a test advances the clock.
		mainClock.autoAdvance = false
		setRevealedTextContent(compositions = mutableListOf())
		waitForIdle()

		onNodeWithText(REVEALED_TEXT).assertTextEquals(REVEALED_TEXT)

		advanceAnimationsBy(MID_REVEAL_MILLIS)

		onNodeWithText(REVEALED_TEXT).assertTextEquals(REVEALED_TEXT)
	}

	@Test
	fun when_textIsRevealed_then_thePartRecomposesOnceAtTheEndAndNotOnEachFrame() = runTuIndiceUiTest {
		val compositions = mutableListOf<String>()
		mainClock.autoAdvance = false
		setRevealedTextContent(compositions = compositions)
		waitForIdle()
		compositions.clear()

		advanceAnimationsBy(AssistantChatMotion.RevealMaxMillis.toLong() * 2)

		assertEquals(
			expected = 1,
			actual = compositions.count { name -> name == REVEALED_TEXT_PROBE_NAME },
			message = "The part must recompose once, when the reveal ends. Reported: $compositions"
		)
		onNodeWithText(REVEALED_TEXT).assertTextEquals(REVEALED_TEXT)
	}

	// The words that have not appeared yet are erased from the drawn text. Before the reveal, the host holds no ink that
	// strong; once the reveal ends, it does, so the check can see the text at all.
	@Test
	fun when_theRevealStarts_then_theWordsNotYetShownAreNotDrawn() = runTuIndiceUiTest {
		mainClock.autoAdvance = false
		setRevealedTextHost()
		waitForIdle()
		mainClock.advanceTimeBy(FRAME_MILLIS)
		waitForIdle()

		assertEquals(0, strongInkPixels(), "no word is drawn before its reveal")

		advanceAnimationsBy(AssistantChatMotion.RevealMaxMillis.toLong() * 2)

		assertTrue(strongInkPixels() > 0, "the whole text is drawn once the reveal ends")
	}
}

@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.setRevealedTextHost() {
	setTuIndiceTestContent {
		CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides true) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.testTag(HOST_TAG)
					.background(Color.White)
					.padding(all = HOST_PADDING)
			) {
				AssistantRevealedTextView(text = REVEALED_TEXT, animate = true)
			}
		}
	}
}

// The pixels of the host that differ strongly from its white corner: the drawn text, not the words still erased.
@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.strongInkPixels(): Int {
	waitForIdle()
	val pixels = onNodeWithTag(HOST_TAG).captureToImage().toPixelMap()
	val background = pixels[0, 0]
	var count = 0

	for (x in 0 until pixels.width) {
		for (y in 0 until pixels.height) {
			if (pixels[x, y].isStronglyDifferentFrom(background)) count++
		}
	}
	return count
}

private fun Color.isStronglyDifferentFrom(other: Color): Boolean =
	abs(red - other.red) > STRONG_INK ||
		abs(green - other.green) > STRONG_INK ||
		abs(blue - other.blue) > STRONG_INK

// Seven words: the reveal lasts 300 ms, so the middle of it falls well inside the frames the test advances.
private const val REVEALED_TEXT = "Una respuesta que llega palabra por palabra"
private const val REVEALED_TEXT_PROBE_NAME = "revealed-text"
private const val MID_REVEAL_MILLIS = 100L
private const val HOST_TAG = "revealed_text_pixel_host"
private val HOST_PADDING = 8.dp
private const val FRAME_MILLIS = 32L

// A word a frame or two into its fade inks well under this share of the full colour; the drawn text is far over it.
private const val STRONG_INK = 0.5f

@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.setRevealedTextContent(compositions: MutableList<String>) {
	setTuIndiceTestContent {
		CompositionLocalProvider(LocalTuIndiceCompositionProbe provides { name -> compositions += name }) {
			AssistantRevealedTextView(text = REVEALED_TEXT, animate = true)
		}
	}
}
