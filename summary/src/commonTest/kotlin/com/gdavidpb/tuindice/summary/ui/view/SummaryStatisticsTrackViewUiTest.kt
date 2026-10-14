package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsTrackViewUiTest {
	@Test
	fun when_fractionIsZero_then_nothingIsFilled() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsTrackView(
				fraction = 0f,
				color = Color.Blue,
				modifier = Modifier.testTag(TRACK_TAG)
			)
		}
		waitForIdle()

		val fill = onNodeWithTag(SummaryUiTags.StatisticsTrackFill, useUnmergedTree = true)
		assertEquals(0, fill.fetchSemanticsNode().size.width)
	}

	@Test
	fun when_fractionIsHalf_then_theFillCoversHalfTheTrack() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsTrackView(
				fraction = 0.5f,
				color = Color.Blue,
				modifier = Modifier.testTag(TRACK_TAG)
			)
		}
		waitForIdle()

		val trackWidth = onNodeWithTag(TRACK_TAG).fetchSemanticsNode().size.width
		val fillWidth = onNodeWithTag(SummaryUiTags.StatisticsTrackFill, useUnmergedTree = true)
			.fetchSemanticsNode().size.width

		// Half of an odd width rounds to a pixel either way, so the fill may differ from the half by one.
		assertTrue(trackWidth / 2 - fillWidth in -1..1, "Fill is ${fillWidth}px of a ${trackWidth}px track")
	}

	private companion object {
		const val TRACK_TAG = "summary_statistics_test_track"
	}
}
