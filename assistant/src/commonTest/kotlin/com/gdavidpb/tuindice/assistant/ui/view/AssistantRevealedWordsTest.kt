package com.gdavidpb.tuindice.assistant.ui.view

import com.gdavidpb.tuindice.assistant.ui.style.AssistantChatMotion
import com.gdavidpb.tuindice.assistant.ui.style.revealStepMillis
import com.gdavidpb.tuindice.assistant.ui.style.revealTotalMillis
import kotlin.test.Test
import kotlin.test.assertEquals

// How much of each word the reveal draws at a moment of it. The part still hidden is erased from the drawn text. These
// pin the hiding the reveal relies on; the drawing itself is checked by AssistantRevealedTextViewUiTest.
class AssistantRevealedWordsTest {
	@Test
	fun when_theRevealStarts_then_everyWordIsStillHidden() {
		val step = revealStepMillis(WORD_COUNT)

		for (index in 0 until WORD_COUNT) {
			assertEquals(0f, wordAlpha(elapsed = 0f, index = index, step = step), "word $index is shown at the start")
		}
	}

	@Test
	fun when_aWordHasNotStarted_then_itIsStillHiddenWhileTheFirstWordIsShown() {
		val step = revealStepMillis(WORD_COUNT)
		val elapsed = AssistantChatMotion.WordFadeMillis.toFloat()

		assertEquals(1f, wordAlpha(elapsed = elapsed, index = 0, step = step), "the first word is shown")
		assertEquals(
			expected = 0f,
			actual = wordAlpha(elapsed = elapsed, index = WORD_COUNT - 1, step = step),
			message = "the last word is shown before it starts"
		)
	}

	@Test
	fun when_theRevealEnds_then_everyWordIsShown() {
		val step = revealStepMillis(WORD_COUNT)
		val total = revealTotalMillis(WORD_COUNT)

		for (index in 0 until WORD_COUNT) {
			assertEquals(
				expected = 1f,
				actual = wordAlpha(elapsed = total, index = index, step = step),
				absoluteTolerance = RATE_TOLERANCE,
				message = "word $index is still hidden at the end"
			)
		}
	}
}

// Seven words, as in the view's own reveal test.
private const val WORD_COUNT = 7
private const val RATE_TOLERANCE = 0.001f
