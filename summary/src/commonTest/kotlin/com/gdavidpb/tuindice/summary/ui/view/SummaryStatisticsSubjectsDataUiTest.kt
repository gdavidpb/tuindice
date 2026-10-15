package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFactItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsOutcome
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsSubjectsItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsSubjectsDataUiTest {
	@Test
	fun when_subjectsAreGiven_then_theFigureAndTheChartAreWritten() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			DataHost()
		}
		waitForIdle()

		assertNodeVisible(SummaryUiTags.StatisticsFigure)
		assertNodeVisible(SummaryUiTags.statisticsChart(SummaryStatisticsChartKey.Subjects))
	}

	@Test
	fun when_theFactsAreGiven_then_theyAreWrittenUnderTheLegend() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			DataHost()
		}
		waitForIdle()

		onNodeWithText("44/46").assertIsDisplayed()
		onNodeWithText("al primer intento").assertIsDisplayed()
	}

	@Composable
	private fun DataHost() {
		Column {
			SummaryStatisticsSubjectsData(
				data = SummaryStatisticsSubjectsItem.Content.Data(
					figure = figureOf(value = "47", label = "materias cursadas"),
					segments = listOf(
						segmentOf(SummaryStatisticsOutcome.APPROVED, 44f, "44 aprobadas", "44 aprobadas · 94 %")
					),
					facts = listOf(SummaryStatisticsFactItem(value = "44/46", label = words("al primer intento"))),
					chartSummary = words("47 materias cursadas")
				),
				chartActionLabel = "Ver detalle"
			)
		}
	}
}
