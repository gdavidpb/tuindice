package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsIndexItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsLinePointItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsTooltipItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsIndexDataUiTest {
	@Test
	fun when_theIndexIsGiven_then_itsGradeCaptionAndChartAreWritten() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			DataHost(best = null, worst = null)
		}
		waitForIdle()

		assertNodeVisible(SummaryUiTags.StatisticsFigure)
		onNodeWithTag(SummaryUiTags.StatisticsCaption).assertIsDisplayed()
		onNodeWithText("al cierre de Ene - Mar 2026").assertIsDisplayed()
		assertNodeVisible(SummaryUiTags.statisticsChart(SummaryStatisticsChartKey.Index))
	}

	@Test
	fun when_theBestAndTheWorstTermsAreGiven_then_theyAreNamedUnderTheChart() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			DataHost(best = "Más alto: Ene - Mar 2024 · 4.5714", worst = "Más bajo: Sep - Dic 2023 · 4.2000")
		}
		waitForIdle()

		onNodeWithText("Más alto: Ene - Mar 2024 · 4.5714").assertIsDisplayed()
		onNodeWithText("Más bajo: Sep - Dic 2023 · 4.2000").assertIsDisplayed()
	}

	@Composable
	private fun DataHost(best: String?, worst: String?) {
		Column {
			SummaryStatisticsIndexData(
				data = SummaryStatisticsIndexItem.Content.Data(
					grade = 4.125f,
					label = words("acumulado"),
					caption = words("al cierre de Ene - Mar 2026"),
					points = listOf(
						SummaryStatisticsLinePointItem(
							axisLabel = words("E-M 24"),
							lineValue = 4.2068f,
							markerValue = 4.5714f,
							tooltip = SummaryStatisticsTooltipItem(title = words("Ene - Mar 2024")),
							isBest = true
						),
						SummaryStatisticsLinePointItem(
							axisLabel = words("S-D 24"),
							lineValue = 4.125f,
							markerValue = 4.1f,
							tooltip = SummaryStatisticsTooltipItem(title = words("Sep - Dic 2024"))
						)
					),
					minValue = 4.1f,
					maxValue = 4.5714f,
					best = best?.let(::words),
					worst = worst?.let(::words),
					chartSummary = words("4.1250 acumulado")
				),
				chartActionLabel = "Ver detalle"
			)
		}
	}
}
