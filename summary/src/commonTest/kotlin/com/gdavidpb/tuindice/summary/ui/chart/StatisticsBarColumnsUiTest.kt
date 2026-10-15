package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.model.RichTooltipText
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class StatisticsBarColumnsUiTest {
	@Test
	fun when_barsAreGiven_then_eachBarStandsInItsOwnColumn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ColumnsHost(bars = listOf(bar(index = 0, value = 2f), bar(index = 1, value = 4f)))
		}
		waitForIdle()

		val first = boundsOf(SummaryUiTags.statisticsFill(KEY, 0))
		val second = boundsOf(SummaryUiTags.statisticsFill(KEY, 1))
		assertEquals(COLUMN_DP, (second.left + second.right) / 2 - (first.left + first.right) / 2, 1f)
	}

	@Test
	fun when_aBarHasAValueLabel_then_itsFigureIsWrittenAboveIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ColumnsHost(bars = listOf(bar(index = 0, value = 2f, valueLabel = "2"), bar(index = 1, value = 4f)))
		}
		waitForIdle()

		onNodeWithTag(StatisticsLabelUiTags.value(KEY, 0), useUnmergedTree = true).assertTextEquals("2")
		onAllNodesWithTag(StatisticsLabelUiTags.value(KEY, 1)).assertCountEquals(0)
	}

	@Composable
	private fun ColumnsHost(bars: List<StatisticsBarPoint>) {
		val openIndex = remember { mutableStateOf<Int?>(null) }

		Box(
			modifier = Modifier
				.testTag(HOST_TAG)
				.width(WIDTH_DP.dp)
				.height(StatisticsChartDefaults.ChartHeight)
		) {
			StatisticsBarColumns(
				bars = bars,
				scale = 4f,
				columnWidth = COLUMN_DP,
				labels = StatisticsChartLabels(key = KEY, summary = "Barras", actionLabel = "Abrir"),
				openIndex = openIndex
			)
		}
	}

	private fun bar(index: Int, value: Float, valueLabel: String? = null) = StatisticsBarPoint(
		label = "Barra $index",
		value = value,
		tooltip = RichTooltipText(title = "Barra $index", lines = emptyList()),
		axisLabel = "B$index",
		valueLabel = valueLabel
	)

	private fun ComposeUiTest.boundsOf(tag: String) =
		onNodeWithTag(testTag = tag, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

	private companion object {
		const val HOST_TAG = "summary_statistics_columns_host"
		const val KEY = "columns_test"
		const val WIDTH_DP = 200
		const val COLUMN_DP = 60f
	}
}
