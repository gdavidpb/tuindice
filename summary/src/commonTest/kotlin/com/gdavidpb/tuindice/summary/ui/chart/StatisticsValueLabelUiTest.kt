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
class StatisticsValueLabelUiTest {
	@Test
	fun when_aValueIsGiven_then_itIsWrittenJustAboveTheTopOfItsBar() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			LabelHost(shownFraction = 0.5f)
		}
		waitForIdle()

		onNodeWithTag(LABEL_TAG, useUnmergedTree = true).assertTextEquals("4.5")
		// The bar's top is the drawn share of the plot above the baseline, and the figure's line lies over it.
		val expectedTop = StatisticsChartDefaults.ChartHeight.value -
			StatisticsChartDefaults.PlotHeight.value * 0.5f -
			StatisticsChartDefaults.ValueLabelHeight.value
		assertEquals(expectedTop, boundsOf(LABEL_TAG).top - boundsOf(HOST_TAG).top, 1f)
	}

	@Test
	fun when_aValueIsWritten_then_itIsHiddenFromTheScreenReader() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			LabelHost(shownFraction = 1f)
		}
		waitForIdle()

		val config = onNodeWithTag(LABEL_TAG, useUnmergedTree = true).fetchSemanticsNode().config
		assertTrue(config.contains(SemanticsProperties.HideFromAccessibility))
	}

	@Composable
	private fun LabelHost(shownFraction: Float) {
		Box(
			modifier = Modifier
				.testTag(HOST_TAG)
				.width(WIDTH_DP.dp)
				.height(StatisticsChartDefaults.ChartHeight)
		) {
			StatisticsValueLabel(
				text = "4.5",
				tag = LABEL_TAG,
				shownFraction = { shownFraction },
				columnWidth = COLUMN_DP
			)
		}
	}

	private fun ComposeUiTest.boundsOf(tag: String) =
		onNodeWithTag(testTag = tag, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

	private companion object {
		const val HOST_TAG = "summary_statistics_value_host"
		const val LABEL_TAG = "summary_statistics_value_label"
		const val WIDTH_DP = 120
		const val COLUMN_DP = 60f
	}
}
