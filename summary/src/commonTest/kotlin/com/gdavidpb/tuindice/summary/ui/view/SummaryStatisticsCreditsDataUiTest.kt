package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCreditsItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsOutcome
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsCreditsDataUiTest {
	@Test
	fun when_creditsAreGiven_then_theFigureAndTheBarAreWritten() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			DataHost(pensum = null)
		}
		waitForIdle()

		assertNodeVisible(SummaryUiTags.StatisticsFigure)
		assertNodeVisible(SummaryUiTags.statisticsChart(SummaryStatisticsChartKey.Credits))
	}

	@Test
	fun when_aPensumIsCached_then_ItsProgressIsWrittenUnderTheBar() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			DataHost(pensum = SummaryStatisticsCreditsItem.Pensum(
				figure = figureOf(value = "152", label = "de 205 según el pensum", trailing = "74 %"),
				fraction = 0.74f
			))
		}
		waitForIdle()

		onNodeWithText("de 205 según el pensum").assertIsDisplayed()
		assertNodeVisible(SummaryUiTags.StatisticsPensumTrack)
	}

	@Composable
	private fun DataHost(pensum: SummaryStatisticsCreditsItem.Pensum?) {
		Column {
			SummaryStatisticsCreditsData(
				data = SummaryStatisticsCreditsItem.Content.Data(
					figure = figureOf(value = "164", label = "créditos cursados según tu registro"),
					segments = listOf(
						segmentOf(SummaryStatisticsOutcome.APPROVED, 152f, "152 aprobados", "152 aprobados · 93 %")
					),
					pensum = pensum,
					chartSummary = words("164 créditos cursados según tu registro")
				),
				chartActionLabel = "Ver detalle"
			)
		}
	}
}
