package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.LocalTuIndiceCompositionProbe
import com.gdavidpb.tuindice.base.ui.ReportComposition
import com.gdavidpb.tuindice.base.ui.model.TuIndiceProgressSegment
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// The progress of a bar is read where the bar is laid out, so a progress that changes on each frame moves the segments
// and composes neither the bar nor the part that holds it.
@OptIn(ExperimentalTestApi::class)
class TuIndiceProgressBarProgressUiTest {
	@Test
	fun when_progressIsGiven_then_eachSegmentKeepsItsPlaceAsTheBarGrows() = runTuIndiceUiTest {
		var progress by mutableStateOf(0.5f)

		setTuIndiceTestContent {
			SegmentsHost(progress = { progress })
		}
		waitForIdle()
		assertSegmentsAt(progress = 0.5f)

		progress = 1f
		waitForIdle()
		assertSegmentsAt(progress = 1f)
	}

	@Test
	fun when_progressChanges_then_theBarMovesWithoutItsCallerComposing() = runTuIndiceUiTest {
		val compositions = mutableListOf<String>()
		val probe: (String) -> Unit = { name -> compositions += name }
		var progress by mutableStateOf(0f)
		mainClock.autoAdvance = false

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceCompositionProbe provides probe) {
				SegmentsHost(progress = { progress })
			}
		}
		waitForIdle()
		val composed = compositions.count { name -> name == HOST_PROBE }
		assertTrue(composed >= 1, "the caller composes for its start")

		progress = 0.5f
		waitForIdle()
		assertSegmentsAt(progress = 0.5f)

		progress = 1f
		waitForIdle()
		assertSegmentsAt(progress = 1f)

		val afterChanges = compositions.count { name -> name == HOST_PROBE }
		assertEquals(composed, afterChanges, "the caller composes for its start only")
	}

	@Composable
	private fun SegmentsHost(progress: () -> Float) {
		ReportComposition(HOST_PROBE)

		TuIndiceProgressBar(
			segments = listOf(
				TuIndiceProgressSegment(fraction = 0.25f, color = Color.Red, tag = FIRST_TAG),
				TuIndiceProgressSegment(fraction = 0.75f, color = Color.Green, tag = SECOND_TAG)
			),
			modifier = Modifier.width(TRACK_PX.dp).testTag(TRACK_TAG),
			progress = progress
		)
	}

	// The first segment is its quarter of the track and the second its three quarters, each shown at the progress. The
	// second starts where the first one ends, at the same progress, so each keeps its place as the bar grows.
	private fun ComposeUiTest.assertSegmentsAt(progress: Float) {
		val track = boundsOf(TRACK_TAG)
		val first = boundsOf(FIRST_TAG)
		val second = boundsOf(SECOND_TAG)
		val firstWidth = TRACK_PX / 4f * progress
		val secondStart = TRACK_PX / 4f * progress
		val secondWidth = TRACK_PX * 3f / 4f * progress

		assertTrue(abs(first.width - firstWidth) <= 1f, "the first segment is ${first.width}px at $progress")
		assertTrue(
			abs(second.left - track.left - secondStart) <= 1f,
			"the second segment starts at ${second.left - track.left}px at $progress"
		)
		assertTrue(abs(second.width - secondWidth) <= 1f, "the second segment is ${second.width}px at $progress")
	}

	private fun ComposeUiTest.boundsOf(tag: String): Rect {
		return onNodeWithTag(testTag = tag, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
	}

	private companion object {
		const val TRACK_TAG = "progress_bar_progress_track"
		const val FIRST_TAG = "progress_bar_progress_first"
		const val SECOND_TAG = "progress_bar_progress_second"
		const val HOST_PROBE = "progress-bar-host"
		const val TRACK_PX = 200
	}
}
