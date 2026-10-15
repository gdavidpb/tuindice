package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.model.RichTooltipText
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class StatisticsStackedTrackUiTest {
	@Test
	fun when_aSegmentHasNoValue_then_itIsNotFilled() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TrackHost(values = listOf(2f, 0f, 1f))
		}
		waitForIdle()

		onAllNodesWithTag(SummaryUiTags.statisticsFill(KEY, 0)).assertCountEquals(1)
		onAllNodesWithTag(SummaryUiTags.statisticsFill(KEY, 1)).assertCountEquals(0)
		onAllNodesWithTag(SummaryUiTags.statisticsFill(KEY, 2)).assertCountEquals(1)
	}

	@Test
	fun when_segmentsHaveValues_then_eachFillIsAsWideAsItsShareOfTheTrack() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TrackHost(values = listOf(1f, 3f))
		}
		waitForIdle()

		assertEquals(TRACK_WIDTH / 4f, boundsOf(SummaryUiTags.statisticsFill(KEY, 0)).width, 1.5f)
		assertEquals(TRACK_WIDTH * 3f / 4f, boundsOf(SummaryUiTags.statisticsFill(KEY, 1)).width, 1.5f)
	}

	@Composable
	private fun TrackHost(values: List<Float>) {
		val openIndex = remember { mutableStateOf<Int?>(null) }
		val segments = values.map { value ->
			StatisticsSegment(
				label = "Segmento",
				value = value,
				color = Color.Blue,
				tooltip = RichTooltipText(title = "Segmento", lines = emptyList())
			)
		}

		Box(
			modifier = Modifier
				.testTag(HOST_TAG)
				.width(TRACK_WIDTH.dp)
				.height(TuIndiceComponentSizes.ProgressBarHeight)
		) {
			StatisticsStackedTrack(
				segments = segments,
				progress = 1f,
				labels = StatisticsChartLabels(key = KEY, summary = "Barra", actionLabel = "Abrir"),
				openIndex = openIndex
			)
		}
	}

	private fun ComposeUiTest.boundsOf(tag: String) =
		onNodeWithTag(testTag = tag, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

	private companion object {
		const val HOST_TAG = "summary_statistics_track_host"
		const val KEY = "track_test"
		const val TRACK_WIDTH = 200
	}
}
