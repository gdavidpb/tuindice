package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.model.RichTooltipText
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalTestApi::class)
class StatisticsLineTraceUiTest {
	@Test
	fun when_pointsAreGiven_then_theTraceFillsTheBandOfTheChart() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TraceHost()
		}
		waitForIdle()

		assertEquals(boundsOf(HOST_TAG).height, boundsOf(TRACE_TAG).height, 1f)
		assertEquals(boundsOf(HOST_TAG).width, boundsOf(TRACE_TAG).width, 1f)
	}

	@Test
	fun when_theTraceIsDrawn_then_itTakesNoTouch() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TraceHost()
		}
		waitForIdle()

		val config = onNodeWithTag(TRACE_TAG, useUnmergedTree = true).fetchSemanticsNode().config
		assertNull(config.getOrNull(SemanticsActions.OnClick))
	}

	@Composable
	private fun TraceHost() {
		Box(
			modifier = Modifier
				.testTag(HOST_TAG)
				.width(WIDTH_DP.dp)
				.height(StatisticsChartDefaults.ChartHeight)
		) {
			StatisticsLineTrace(
				points = listOf(point(lineValue = 4f), point(lineValue = 4.5f)),
				range = 4f..5f,
				columnWidth = COLUMN_DP,
				modifier = Modifier.testTag(TRACE_TAG)
			)
		}
	}

	private fun point(lineValue: Float) = StatisticsLinePoint(
		label = "Punto",
		lineValue = lineValue,
		tooltip = RichTooltipText(title = "Punto", lines = emptyList()),
		axisLabel = "P"
	)

	private fun ComposeUiTest.boundsOf(tag: String) =
		onNodeWithTag(testTag = tag, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

	private companion object {
		const val HOST_TAG = "summary_statistics_trace_host"
		const val TRACE_TAG = "summary_statistics_trace"
		const val WIDTH_DP = 120
		const val COLUMN_DP = 60f
	}
}
