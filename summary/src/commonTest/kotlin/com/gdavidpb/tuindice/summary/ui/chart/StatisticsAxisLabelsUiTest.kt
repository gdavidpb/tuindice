package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class StatisticsAxisLabelsUiTest {
	@Test
	fun when_labelsAreGiven_then_eachIsWrittenUnderItsColumnInTheBandBelowThePlot() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AxisHost(labels = listOf("E-M 24", "S-D 24", "E-M 25"))
		}
		waitForIdle()

		onNodeWithTag(axisTag(1), useUnmergedTree = true).assertTextEquals("S-D 24")
		// Each label starts one column after the last, and the band starts where the plot ends, one chart height down.
		assertEquals(COLUMN_PX, boundsOf(axisTag(1)).left - boundsOf(axisTag(0)).left, 1f)
		assertEquals(PLOT_PX, boundsOf(axisTag(0)).top - boundsOf(HOST_TAG).top, 1f)
	}

	@Test
	fun when_aLabelIsWritten_then_itIsHiddenFromTheScreenReader() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AxisHost(labels = listOf("E-M 24"))
		}
		waitForIdle()

		val config = onNodeWithTag(axisTag(0), useUnmergedTree = true).fetchSemanticsNode().config
		assertTrue(config.contains(SemanticsProperties.HideFromAccessibility))
	}

	@Composable
	private fun AxisHost(labels: List<String>) {
		Box(
			modifier = Modifier
				.testTag(HOST_TAG)
				.width(WIDTH_DP.dp)
				.height(StatisticsChartDefaults.ContentHeight)
		) {
			StatisticsAxisLabels(key = CHART_KEY, labels = labels, columnWidth = COLUMN_PX)
		}
	}

	// The position of a node in the window, in pixels, read from the unmerged tree.
	private fun ComposeUiTest.boundsOf(tag: String) =
		onNodeWithTag(testTag = tag, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

	private fun axisTag(index: Int): String = StatisticsLabelTags.axis(CHART_KEY, index)

	private companion object {
		const val CHART_KEY = "axis_test"
		const val HOST_TAG = "summary_statistics_axis_host"
		const val WIDTH_DP = 200
		const val COLUMN_PX = 48f

		// The plot is the chart's height, which the band under it starts at, at a density of 1.
		const val PLOT_PX = 168f
	}
}
