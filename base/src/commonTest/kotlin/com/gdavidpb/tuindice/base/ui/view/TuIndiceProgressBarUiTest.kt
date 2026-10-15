package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.model.TuIndiceProgressSegment
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class TuIndiceProgressBarUiTest {
	@Test
	fun when_oneColour_then_theFillIsItsShareOfTheTrack() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TuIndiceProgressBar(
				fraction = 0.5f,
				color = Color.Blue,
				modifier = Modifier.testTag(TRACK_TAG),
				fillTag = FILL_TAG
			)
		}
		waitForIdle()

		val track = onNodeWithTag(TRACK_TAG).fetchSemanticsNode().size.width
		val fill = onNodeWithTag(FILL_TAG, useUnmergedTree = true).fetchSemanticsNode().size.width

		// Half of an odd width rounds to a pixel either way, so the fill may differ from the half by one.
		assertTrue(abs(fill - track / 2f) <= 1f, "the fill is ${fill}px of a ${track}px track, not half of it")
	}

	@Test
	fun when_oneColourHasNoShare_then_nothingIsFilled() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TuIndiceProgressBar(fraction = 0f, color = Color.Blue, fillTag = FILL_TAG)
		}
		waitForIdle()

		assertEquals(0, onNodeWithTag(FILL_TAG, useUnmergedTree = true).fetchSemanticsNode().size.width)
	}

	@Test
	fun when_segmented_then_eachSegmentStartsWhereTheLastOneEnds() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TuIndiceProgressBar(
				segments = listOf(
					TuIndiceProgressSegment(fraction = 0.25f, color = Color.Red, tag = FIRST_TAG),
					TuIndiceProgressSegment(fraction = 0.75f, color = Color.Green, tag = SECOND_TAG)
				),
				modifier = Modifier.testTag(TRACK_TAG)
			)
		}
		waitForIdle()

		val track = onNodeWithTag(TRACK_TAG).fetchSemanticsNode().boundsInRoot
		val first = onNodeWithTag(FIRST_TAG, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val second = onNodeWithTag(SECOND_TAG, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		assertTrue(abs(first.width - track.width / 4f) <= 1f, "the first segment is a quarter of the track")
		assertTrue(abs(second.left - first.right) <= 1f, "the second segment starts where the first one ends")
		assertTrue(abs(second.right - track.right) <= 1f, "the second segment ends at the end of the track")
	}

	@Test
	fun when_segmentedWithAGap_then_theSegmentsAreApartByIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TuIndiceProgressBar(
				segments = listOf(
					TuIndiceProgressSegment(fraction = 0.5f, color = Color.Red, tag = FIRST_TAG),
					TuIndiceProgressSegment(fraction = 0.5f, color = Color.Green, tag = SECOND_TAG)
				),
				modifier = Modifier.testTag(TRACK_TAG),
				gap = GAP
			)
		}
		waitForIdle()

		val first = onNodeWithTag(FIRST_TAG, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val second = onNodeWithTag(SECOND_TAG, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		// The test scene is one pixel to the dp, as the stacked bar's test assumes, so the gap is its dp in pixels.
		assertTrue(abs(second.left - first.right - GAP.value) <= 1f, "the segments are $GAP apart")
	}

	@Test
	fun when_rendered_then_itIsSixDpTall() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TuIndiceProgressBar(fraction = 0.5f, color = Color.Blue, modifier = Modifier.testTag(TRACK_TAG))
		}
		waitForIdle()

		// The height is the contract every bar of the app shares, so it is written out and not read from the token.
		onNodeWithTag(TRACK_TAG).assertHeightIsEqualTo(BAR_HEIGHT)
	}

	private companion object {
		const val TRACK_TAG = "progress_bar_test_track"
		const val FILL_TAG = "progress_bar_test_fill"
		const val FIRST_TAG = "progress_bar_test_first"
		const val SECOND_TAG = "progress_bar_test_second"
		val GAP = 6.dp
		val BAR_HEIGHT = 6.dp
	}
}
