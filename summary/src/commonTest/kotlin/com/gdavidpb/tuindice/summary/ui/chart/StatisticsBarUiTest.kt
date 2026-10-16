package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class StatisticsBarUiTest {
	@Test
	fun when_aShareIsGiven_then_itRisesToThatShareOfThePlot() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			BarHost(shownFraction = 0.5f)
		}
		waitForIdle()

		assertEquals(StatisticsChartDefaults.PlotHeight.value * 0.5f, boundsOf(BAR_TAG).height, 1f)
	}

	@Test
	fun when_aBarIsDrawn_then_itStandsOnTheBaselineOfTheChart() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			BarHost(shownFraction = 1f)
		}
		waitForIdle()

		assertEquals(boundsOf(HOST_TAG).bottom, boundsOf(BAR_TAG).bottom, 1f)
	}

	@Composable
	private fun BarHost(shownFraction: Float) {
		Box(
			modifier = Modifier
				.testTag(HOST_TAG)
				.width(WIDTH_DP.dp)
				.height(StatisticsChartDefaults.ChartHeight)
		) {
			StatisticsBar(
				shownFraction = shownFraction,
				width = BAR_WIDTH_DP,
				color = Color.Blue,
				tag = BAR_TAG
			)
		}
	}

	private fun ComposeUiTest.boundsOf(tag: String) =
		onNodeWithTag(testTag = tag, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

	private companion object {
		const val HOST_TAG = "summary_statistics_bar_host"
		const val BAR_TAG = "summary_statistics_bar"
		const val WIDTH_DP = 120
		const val BAR_WIDTH_DP = 16f
	}
}
