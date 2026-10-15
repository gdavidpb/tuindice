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
import com.gdavidpb.tuindice.base.ui.model.RichTooltipText
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class StatisticsLineMarkersUiTest {
	@Test
	fun when_aPointIsGiven_then_itsMarkerSitsOnItsColumnAtItsMarkerValue() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			MarkersHost(points = listOf(point(index = 0, markerValue = 4f), point(index = 1, markerValue = 5f)))
		}
		waitForIdle()

		// The second point is the highest of the range, so its marker sits at the top of the band, half a target down.
		val target = boundsOf(SummaryUiTags.statisticsTarget(KEY, 1))
		val host = boundsOf(HOST_TAG)
		assertEquals(COLUMN_DP * 1.5f, (target.left + target.right) / 2 - host.left, 1f)
		assertEquals(StatisticsChartDefaults.LineInset.value, (target.top + target.bottom) / 2 - host.top, 1f)
	}

	@Test
	fun when_aPointHasARing_then_theRingIsDrawnAroundItsMarker() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			MarkersHost(points = listOf(point(index = 0, markerValue = 4f, ringColor = Color.Red), point(index = 1, markerValue = 5f)))
		}
		waitForIdle()

		assertNodeVisible(StatisticsLabelUiTags.ring(KEY, 0), useUnmergedTree = true)
		onAllNodesWithTag(StatisticsLabelUiTags.ring(KEY, 1)).assertCountEquals(0)
	}

	@Composable
	private fun MarkersHost(points: List<StatisticsLinePoint>) {
		val openIndex = remember { mutableStateOf<Int?>(null) }

		Box(
			modifier = Modifier
				.testTag(HOST_TAG)
				.width(WIDTH_DP.dp)
				.height(StatisticsChartDefaults.ChartHeight)
		) {
			StatisticsLineMarkers(
				points = points,
				range = 4f..5f,
				columnWidth = COLUMN_DP,
				labels = StatisticsChartLabels(key = KEY, summary = "Índice", actionLabel = "Abrir"),
				openIndex = openIndex
			)
		}
	}

	private fun point(index: Int, markerValue: Float, ringColor: Color? = null) = StatisticsLinePoint(
		label = "Punto $index",
		lineValue = markerValue,
		tooltip = RichTooltipText(title = "Punto $index", lines = emptyList()),
		axisLabel = "P$index",
		markerValue = markerValue,
		ringColor = ringColor
	)

	private fun ComposeUiTest.boundsOf(tag: String) =
		onNodeWithTag(testTag = tag, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

	private companion object {
		const val HOST_TAG = "summary_statistics_markers_host"
		const val KEY = "markers_test"
		const val WIDTH_DP = 200
		const val COLUMN_DP = 60f
	}
}
