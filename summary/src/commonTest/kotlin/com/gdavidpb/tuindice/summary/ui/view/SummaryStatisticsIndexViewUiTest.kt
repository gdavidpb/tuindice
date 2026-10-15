package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsIndexItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsLinePointItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsLabelUiTags
import com.gdavidpb.tuindice.summary.ui.chart.assertTooltipShows
import com.gdavidpb.tuindice.testkit.ui.advanceAnimationsBy
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
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
		advanceAnimationsBy(1_000)

		onNodeWithTag(SummaryUiTags.StatisticsFigure).assertTextEquals("4.1250 / 5.0")
		onNodeWithText("acumulado").assertIsDisplayed()
		onNodeWithTag(SummaryUiTags.StatisticsCaption).assertTextEquals("al cierre de Ene - Mar 2026")
		onNodeWithTag(
			testTag = StatisticsLabelUiTags.axis(SummaryStatisticsChartKey.Index, 1),
			useUnmergedTree = true
		).assertTextEquals("E-M 24")
		onNodeWithText("Más alto: Ene - Mar 2024 · 4.5714").assertIsDisplayed()
		assertNodeVisible(SummaryUiTags.StatisticsBestTerm)
		assertNodeVisible(StatisticsLabelUiTags.ring(SummaryStatisticsChartKey.Index, 1), useUnmergedTree = true)
		onAllNodesWithTag(
			testTag = StatisticsLabelUiTags.ring(SummaryStatisticsChartKey.Index, 0),
			useUnmergedTree = true
		).assertCountEquals(0)
	}

	@Test
	fun when_theWorstTermIsNamed_then_itsNoteIsWrittenAndItsPointIsRinged() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsIndexView(item = indexWithTerms(worstIsNamed = true), chartActionLabel = "Ver detalle")
		}
		waitForIdle()

		onNodeWithText("Más bajo: Sep - Dic 2023 · 4.2000").assertIsDisplayed()
		assertNodeVisible(SummaryUiTags.StatisticsWorstTerm)
		assertNodeVisible(StatisticsLabelUiTags.ring(SummaryStatisticsChartKey.Index, 0), useUnmergedTree = true)
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

	private fun indexWithTerms(worstIsNamed: Boolean = false): SummaryStatisticsIndexItem {
		return SummaryStatisticsIndexItem(
			title = words("Índice"),
			content = SummaryStatisticsIndexItem.Content.Data(
				grade = 4.125f,
				label = words("acumulado"),
				caption = words("al cierre de Ene - Mar 2026"),
				points = listOf(
					point(axisLabel = "S-D 23", title = "Sep - Dic 2023", line = 4.1f, marker = 4.2f)
						.copy(isWorst = worstIsNamed),
					point(axisLabel = "E-M 24", title = "Ene - Mar 2024", line = 4.2068f, marker = 4.5714f)
						.copy(isBest = true),
					point(axisLabel = "S-D 24", title = "Sep - Dic 2024", line = 4.125f, marker = 4.1f)
				),
				minValue = 4.1f,
				maxValue = 4.5714f,
				best = words("Más alto: Ene - Mar 2024 · 4.5714"),
				worst = if (worstIsNamed) words("Más bajo: Sep - Dic 2023 · 4.2000") else null,
				chartSummary = words("4.1250 acumulado")
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
