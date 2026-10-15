package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsEvaluationTypeItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsTooltipItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.chart.assertTooltipShows
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsTypeRowUiTest {
	@Test
	fun when_aTypeHasNoGrade_then_itIsWrittenWithoutATarget() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RowHost(tooltip = null)
		}
		waitForIdle()

		onNodeWithText("Quices").assertIsDisplayed()
		onAllNodesWithTag(SummaryUiTags.statisticsTarget(SummaryStatisticsChartKey.CurrentTerm, 0)).assertCountEquals(0)
	}

	@Test
	fun when_aTypeHasAGrade_then_tappingItOpensItsTooltip() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RowHost(tooltip = tooltipOf("Quices: 8 de 10 (80 %)", "1 de 1 calificada"))
		}
		waitForIdle()

		onNodeWithTag(SummaryUiTags.statisticsTarget(SummaryStatisticsChartKey.CurrentTerm, 0)).performClick()

		assertTooltipShows("Quices: 8 de 10 (80 %)")
	}

	@Composable
	private fun RowHost(tooltip: SummaryStatisticsTooltipItem?) {
		val openRow = remember { mutableStateOf<Int?>(null) }

		Column {
			SummaryStatisticsTypeRow(
				index = 0,
				row = SummaryStatisticsEvaluationTypeItem(
					name = words("Quices"),
					gradedText = words("1 de 1 calificada"),
					scoreText = words("8 de 10 · 80 %"),
					fraction = 0.8f,
					tooltip = tooltip
				),
				openRow = openRow,
				chartActionLabel = "Ver detalle"
			)
		}
	}
}
