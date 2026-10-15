package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAnimation
import com.gdavidpb.tuindice.testkit.ui.advanceAnimationsBy
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ShareableTermCardCrossfadeUiTest {
	@Test
	fun when_theTargetChangesWithAnimations_then_theCardBeforeStaysUnderTheNewOneUntilTheFadeEnds() = runTuIndiceUiTest {
		val name = mutableStateOf(FIRST)

		mainClock.autoAdvance = false
		setNamedCards(name)
		name.value = SECOND
		advanceFrames(FRAMES_UNTIL_THE_CHANGE_SHOWS)

		// Mid-fade both cards are composed: the one before and the one it fades to.
		onAllNodesWithText(FIRST).assertCountEquals(1)
		onAllNodesWithText(SECOND).assertCountEquals(1)

		advanceAnimationsBy(FADE_WITH_MARGIN_MILLIS)

		onAllNodesWithText(FIRST).assertCountEquals(0)
		onAllNodesWithText(SECOND).assertCountEquals(1)
	}

	@Test
	fun when_theTargetChangesWithAnimations_then_nothingInTheCardMoves() = runTuIndiceUiTest {
		val name = mutableStateOf(FIRST)

		mainClock.autoAdvance = false
		setNamedCards(name)
		val start = onNodeWithTag(CARD_TAG).fetchSemanticsNode().boundsInRoot
		name.value = SECOND
		advanceFrames(FRAMES_UNTIL_THE_CHANGE_SHOWS)

		// Mid-fade both cards are on screen, and each of them sits where the card sat before the change.
		onAllNodesWithTag(CARD_TAG).assertCountEquals(2)
		assertCardsAt(start)

		advanceAnimationsBy(TuIndiceAnimation.CrossfadeMillis.toLong() / 2)

		onAllNodesWithTag(CARD_TAG).assertCountEquals(2)
		assertCardsAt(start)

		advanceAnimationsBy(FADE_WITH_MARGIN_MILLIS)

		onAllNodesWithTag(CARD_TAG).assertCountEquals(1)
		assertCardsAt(start)
	}

	@Test
	fun when_theTargetChangesAgainDuringAFade_then_thereAreNeverMoreThanTwoCardsOnScreen() = runTuIndiceUiTest {
		val name = mutableStateOf(FIRST)

		mainClock.autoAdvance = false
		setNamedCards(name)
		name.value = SECOND
		advanceFrames(FRAMES_UNTIL_THE_CHANGE_SHOWS)
		name.value = THIRD

		var peak = 0
		repeat(FRAMES_UNTIL_SETTLED) {
			advanceAnimationsBy(ONE_FRAME_MILLIS)

			val onScreen = onAllNodesWithTag(CARD_TAG).fetchSemanticsNodes().size
			peak = maxOf(peak, onScreen)
			assertTrue(onScreen <= 2, "$onScreen cards are on screen at once.")
		}

		// The fades ran, one after the other: at some frame two cards were on screen, and never three.
		assertEquals(2, peak)
		onAllNodesWithTag(CARD_TAG).assertCountEquals(1)
		onAllNodesWithText(THIRD).assertCountEquals(1)
	}

	@Test
	fun when_theTargetChangesWithoutAnimations_then_theNewCardIsTheOnlyOneAtOnce() = runTuIndiceUiTest {
		val name = mutableStateOf(FIRST)

		mainClock.autoAdvance = false
		setNamedCards(name, animated = false)
		name.value = SECOND
		advanceFrames(FRAMES_UNTIL_THE_CHANGE_SHOWS)

		onAllNodesWithText(FIRST).assertCountEquals(0)
		onAllNodesWithText(SECOND).assertCountEquals(1)
	}

	@Test
	fun when_theCardHadNothingBefore_then_itIsDrawnAtOnceAndNotFaded() = runTuIndiceUiTest {
		val name = mutableStateOf("")

		mainClock.autoAdvance = false
		setNamedCards(name)
		name.value = FIRST
		advanceFrames(FRAMES_UNTIL_THE_CHANGE_SHOWS)

		// Each pixel is laid over white, so a transparent one counts as white. The darkest pixel is the ink of the text,
		// which is near black at full strength and a pale grey a few frames into a fade.
		val pixels = onNodeWithTag(CARD_TAG).captureToImage().toPixelMap()
		val darkest = (0 until pixels.height).minOf { row ->
			(0 until pixels.width).minOf { column ->
				val colour = pixels[column, row]
				val luminance = (colour.red + colour.green + colour.blue) / 3f

				luminance * colour.alpha + (1f - colour.alpha)
			}
		}

		assertTrue(darkest < FULL_INK_LUMINANCE_LIMIT, "The card faded in over nothing: its ink is pale.")
	}

	private fun ComposeUiTest.setNamedCards(name: MutableState<String>, animated: Boolean = true) {
		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides animated) {
				ShareableTermCardCrossfade(
					targetState = name.value,
					modifier = Modifier.fillMaxSize(),
					isEmpty = { card -> card.isEmpty() }
				) { card ->
					Text(text = card, modifier = Modifier.testTag(CARD_TAG))
				}
			}
		}
	}

	private fun ComposeUiTest.advanceFrames(frames: Int) {
		repeat(frames) {
			advanceAnimationsBy(ONE_FRAME_MILLIS)
		}
	}

	// Every card on screen, the one before and the one faded to, sits where the card sat before the change.
	private fun ComposeUiTest.assertCardsAt(expected: Rect) {
		onAllNodesWithTag(CARD_TAG).fetchSemanticsNodes().forEach { card ->
			assertEquals(expected.left, card.boundsInRoot.left, absoluteTolerance = POSITION_TOLERANCE_PX)
			assertEquals(expected.top, card.boundsInRoot.top, absoluteTolerance = POSITION_TOLERANCE_PX)
		}
	}

	private companion object {
		const val CARD_TAG = "crossfade_card"
		const val FIRST = "Primera"
		const val SECOND = "Segunda"
		const val THIRD = "Tercera"

		// The clock of a test frame by frame: one frame at 60 Hz.
		const val ONE_FRAME_MILLIS = 16L

		// A change reaches the card a frame after it is made, and the card it fades to is composed a frame or two later:
		// four frames let the change reach the screen, with its fade begun, before a test looks at the card.
		const val FRAMES_UNTIL_THE_CHANGE_SHOWS = 4

		// The fade ends a little after its duration, on the frame after the one it is due on.
		const val FADE_WITH_MARGIN_MILLIS = TuIndiceAnimation.CrossfadeMillis + 2L * ONE_FRAME_MILLIS

		// Long enough for two fades to run one after the other, a frame at a time.
		const val FRAMES_UNTIL_SETTLED = 60

		// The ink of the text at full strength is near black, a luminance of 0 to 1; a frame into a fade it is far paler.
		const val FULL_INK_LUMINANCE_LIMIT = 0.5f

		const val POSITION_TOLERANCE_PX = 0.5f
	}
}
