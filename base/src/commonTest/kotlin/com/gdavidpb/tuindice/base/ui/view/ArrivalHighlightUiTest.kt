package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
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

	@Test
	fun when_triggerIsSet_then_theBorderLiesOnTheContainersOwnEdge_andNotInTheMarginAroundIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			MarginedContainer(trigger = ARRIVAL)
		}

		advanceAnimationsBy(FADE_IN_MILLIS)

		assertEquals(Accent, pixelAtDp(xDp = MarginDp + BorderDp / 2), "the border is drawn on the container's edge")
		// Where a border drawn on the padded bounds would sit, at the outer edge of the margin.
		assertEquals(Backdrop, pixelAtDp(xDp = BorderDp / 2), "the outer edge of the margin stays clear")
	}

	@Test
	fun when_triggerIsNull_then_noBorderIsDrawnOnTheContainer() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			MarginedContainer(trigger = null)
		}

		advanceAnimationsBy(FADE_IN_MILLIS)

		assertEquals(Backdrop, pixelAtDp(xDp = MarginDp + BorderDp / 2), "nothing is drawn on the container's edge")
	}

	// A container of known size inside a margin, on a backdrop of its own colour, so the border and the margin
	// around it can be told apart by the pixels they paint.
	@Composable
	private fun MarginedContainer(trigger: Any?) {
		MaterialTheme(colorScheme = lightColorScheme(primary = Accent)) {
			Box(
				modifier = Modifier
					.testTag(HostTag)
					.background(Backdrop)
					.padding(MarginDp.dp)
			) {
				Box(
					modifier = Modifier
						.size(ContainerDp.dp)
						.arrivalHighlight(trigger = trigger, shape = RectangleShape) {}
				)
			}
		}
	}

	// The pixel of the host's capture at [xDp] from its left edge, at the middle of its height. The test runs at
	// density 1, so a dp is a pixel: the root of the content is 360 x 640 pixels and a denser one would not fit.
	private fun ComposeUiTest.pixelAtDp(xDp: Float): Color {
		waitForIdle()

		val pixels = onNodeWithTag(HostTag).captureToImage().toPixelMap()

		return pixels[xDp.toInt(), pixels.height / 2]
	}

	private companion object {
		const val HighlightTag = "arrival_highlight"
		const val HostTag = "arrival_highlight_host"
		const val ARRIVAL = "arrival-1"
		const val NEXT_ARRIVAL = "arrival-2"
		const val FULL_HIGHLIGHT_MILLIS = 1_600L
		const val FADE_IN_MILLIS = 300L
		const val MarginDp = 16f
		const val ContainerDp = 40f
		const val BorderDp = 2f
		val Accent = Color.Red
		val Backdrop = Color.White
	}
}
