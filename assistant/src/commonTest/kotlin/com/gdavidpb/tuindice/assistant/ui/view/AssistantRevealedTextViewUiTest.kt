package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.ui.style.AssistantChatMotion
import com.gdavidpb.tuindice.base.ui.LocalTuIndiceCompositionProbe
import com.gdavidpb.tuindice.testkit.ui.advanceAnimationsBy
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class AssistantRevealedTextViewUiTest {
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
}

// Seven words: the reveal lasts 300 ms, so the middle of it falls well inside the frames the test advances.
private const val REVEALED_TEXT = "Una respuesta que llega palabra por palabra"
private const val REVEALED_TEXT_PROBE_NAME = "revealed-text"
private const val MID_REVEAL_MILLIS = 100L

@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.setRevealedTextContent(compositions: MutableList<String>) {
	setTuIndiceTestContent {
		CompositionLocalProvider(LocalTuIndiceCompositionProbe provides { name -> compositions += name }) {
			AssistantRevealedTextView(text = REVEALED_TEXT, animate = true)
		}
	}
}
