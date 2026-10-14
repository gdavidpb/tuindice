package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsIndexItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsLinePointItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsLabelTags
import com.gdavidpb.tuindice.summary.ui.chart.assertTooltipShows
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsIndexViewUiTest {
	@Test
	fun when_theIndexHasTerms_then_itsFigureCaptionAxisLabelsAndNotesAreWritten() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsIndexView(item = indexWithTerms(), chartActionLabel = "Ver detalle")
		}
		waitForIdle()

		onNodeWithText("4.1250").assertIsDisplayed()
		onNodeWithText("acumulado").assertIsDisplayed()
		onNodeWithTag(SummaryUiTags.StatisticsCaption).assertTextEquals("al cierre de Ene - Mar 2026")
		onNodeWithTag(
			testTag = StatisticsLabelTags.axis(SummaryStatisticsChartKey.Index, 1),
			useUnmergedTree = true
		).assertTextEquals("E-M 24")
		onNodeWithText("Mejor: Ene - Mar 2024 · 4.5714").assertIsDisplayed()
	}

	@Test
	fun when_theIndexIsEmpty_then_itsTextIsWrittenInPlaceOfTheChart() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsIndexView(
				item = SummaryStatisticsIndexItem(
					title = words("Índice"),
					content = SummaryStatisticsIndexItem.Content.Empty(
						text = words("Tu índice aparecerá cuando termines tu primer trimestre.")
					)
				),
				chartActionLabel = "Ver detalle"
			)
		}

		onNodeWithText("Tu índice aparecerá cuando termines tu primer trimestre.").assertIsDisplayed()
	}

	@Test
	fun when_aPointIsTapped_then_itsTooltipShowsTheTermAndItsIndex() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsIndexView(item = indexWithTerms(), chartActionLabel = "Ver detalle")
		}
		waitForIdle()

		onNodeWithTag(SummaryUiTags.statisticsTarget(SummaryStatisticsChartKey.Index, 1)).performClick()

		assertTooltipShows("Ene - Mar 2024")
	}

	private fun indexWithTerms(): SummaryStatisticsIndexItem {
		return SummaryStatisticsIndexItem(
			title = words("Índice"),
			content = SummaryStatisticsIndexItem.Content.Data(
				figure = figureOf(value = "4.1250", label = "acumulado"),
				caption = words("al cierre de Ene - Mar 2026"),
				points = listOf(
					point(axisLabel = "S-D 23", title = "Sep - Dic 2023", line = 4.1f, marker = 4.2f),
					point(axisLabel = "E-M 24", title = "Ene - Mar 2024", line = 4.2068f, marker = 4.5714f),
					point(axisLabel = "S-D 24", title = "Sep - Dic 2024", line = 4.125f, marker = 4.1f)
				),
				minValue = 4.1f,
				maxValue = 4.5714f,
				best = words("Mejor: Ene - Mar 2024 · 4.5714"),
				worst = null
			)
		)
	}

	private fun point(axisLabel: String, title: String, line: Float, marker: Float): SummaryStatisticsLinePointItem {
		return SummaryStatisticsLinePointItem(
			axisLabel = words(axisLabel),
			lineValue = line,
			markerValue = marker,
			tooltip = tooltipOf(title, "Índice $marker · acumulado $line")
		)
	}
}
