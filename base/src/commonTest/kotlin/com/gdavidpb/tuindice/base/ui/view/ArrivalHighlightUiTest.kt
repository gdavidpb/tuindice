package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.testkit.ui.advanceAnimationsBy
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// The highlight runs on the test clock: 250 ms in, 700 ms held, 600 ms out, so it ends at 1550 ms.
@OptIn(ExperimentalTestApi::class)
class ArrivalHighlightUiTest {
	@Test
	fun when_triggerIsSet_then_playsTheHighlightAndReportsItsEnd() = runTuIndiceUiTest {
		val finished = mutableStateOf(false)

		setTuIndiceTestContent(density = 1f) {
			Box(
				modifier = Modifier
					.size(40.dp)
					.testTag(HighlightTag)
					.arrivalHighlight(trigger = ARRIVAL, shape = RectangleShape) { finished.value = true }
			)
		}

		assertNodeVisible(HighlightTag)
		advanceAnimationsBy(FULL_HIGHLIGHT_MILLIS)
		waitForIdle()
		assertTrue(finished.value)
	}

	@Test
	fun when_animationsAreDisabled_then_finishesAtOnceWithoutWaiting() = runTuIndiceUiTest {
		val finished = mutableStateOf(false)

		setTuIndiceTestContent(density = 1f) {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				Box(
					modifier = Modifier
						.size(40.dp)
						.testTag(HighlightTag)
						.arrivalHighlight(trigger = ARRIVAL, shape = RectangleShape) { finished.value = true }
				)
			}
		}

		waitForIdle()
		assertTrue(finished.value)
	}

	@Test
	fun when_triggerIsNull_then_neverStartsNorFinishes() = runTuIndiceUiTest {
		val finished = mutableStateOf(false)

		setTuIndiceTestContent(density = 1f) {
			Box(
				modifier = Modifier
					.size(40.dp)
					.testTag(HighlightTag)
					.arrivalHighlight(trigger = null, shape = RectangleShape) { finished.value = true }
			)
		}

		advanceAnimationsBy(3_000)
		waitForIdle()
		assertFalse(finished.value)
	}

	@Test
	fun when_triggerChangesAfterFinishing_then_playsAgain() = runTuIndiceUiTest {
		val trigger = mutableStateOf<Any?>(ARRIVAL)
		val finishedCount = mutableStateOf(0)

		setTuIndiceTestContent(density = 1f) {
			Box(
				modifier = Modifier
					.size(40.dp)
					.testTag(HighlightTag)
					.arrivalHighlight(trigger = trigger.value, shape = RectangleShape) {
						finishedCount.value += 1
					}
			)
		}

		advanceAnimationsBy(FULL_HIGHLIGHT_MILLIS)
		waitForIdle()
		assertEquals(expected = 1, actual = finishedCount.value)

		runOnIdle { trigger.value = NEXT_ARRIVAL }
		advanceAnimationsBy(FULL_HIGHLIGHT_MILLIS)
		waitForIdle()
		assertEquals(expected = 2, actual = finishedCount.value)
	}

	private companion object {
		const val HighlightTag = "arrival_highlight"
		const val ARRIVAL = "arrival-1"
		const val NEXT_ARRIVAL = "arrival-2"
		const val FULL_HIGHLIGHT_MILLIS = 1_600L
	}
}
