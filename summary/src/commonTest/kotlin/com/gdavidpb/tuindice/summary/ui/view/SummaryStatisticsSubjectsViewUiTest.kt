package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFactItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsOutcome
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsSubjectsItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.chart.assertTooltipShows
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsSubjectsViewUiTest {
	@Test
	fun when_theSubjectsHaveResults_then_theFigureLegendAndFactsAreWritten() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsSubjectsView(item = subjectsWith(noResult = 0), chartActionLabel = "Ver detalle")
		}

		onNodeWithText("47").assertIsDisplayed()
		onNodeWithText("materias cursadas").assertIsDisplayed()
		onNodeWithText("44 aprobadas").assertIsDisplayed()
		onNodeWithText("44 / 46").assertIsDisplayed()
		onNodeWithText("al primer intento").assertIsDisplayed()
		onNodeWithText("0").assertIsDisplayed()
		onNodeWithText("repetidas").assertIsDisplayed()
		onAllNodesWithText("sin resultado", substring = true).assertCountEquals(0)
	}

	@Test
	fun when_someSubjectsHaveNoResult_then_theirFactIsWrittenWithTheOthers() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsSubjectsView(item = subjectsWith(noResult = 2), chartActionLabel = "Ver detalle")
		}

		onNodeWithText("sin resultado").assertIsDisplayed()
		onNodeWithText("2").assertIsDisplayed()
		onNodeWithText("repetidas").assertIsDisplayed()
	}

	@Test
	fun when_aSegmentIsTapped_then_itsTooltipShowsItsCountAndItsShare() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsSubjectsView(item = subjectsWith(noResult = 0), chartActionLabel = "Ver detalle")
		}
		waitForIdle()

		onNodeWithTag(SummaryUiTags.statisticsTarget(SummaryStatisticsChartKey.Subjects, 1)).performClick()

		assertTooltipShows("2 reprobadas · 4 %")
	}

	private fun subjectsWith(noResult: Int): SummaryStatisticsSubjectsItem {
		return SummaryStatisticsSubjectsItem(
			title = words("Materias"),
			content = SummaryStatisticsSubjectsItem.Content.Data(
				figure = figureOf(value = "47", label = "materias cursadas"),
				segments = listOf(
					segmentOf(SummaryStatisticsOutcome.APPROVED, 44f, "44 aprobadas", "44 aprobadas · 94 %"),
					segmentOf(SummaryStatisticsOutcome.FAILED, 2f, "2 reprobadas", "2 reprobadas · 4 %"),
					segmentOf(SummaryStatisticsOutcome.WITHDRAWN, 1f, "1 retirada", "1 retirada · 2 %")
				),
				facts = listOfNotNull(
					SummaryStatisticsFactItem(value = "44 / 46", label = words("al primer intento")),
					SummaryStatisticsFactItem(value = "0", label = words("repetidas")),
					SummaryStatisticsFactItem(value = "$noResult", label = words("sin resultado"))
						.takeIf { noResult > 0 }
				),
				chartSummary = words("47 materias cursadas")
			)
		)
	}
}
