package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.presentation.model.UiText
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
	fun when_theSubjectsHaveResults_then_theFigureLegendAndNotesAreWritten() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsSubjectsView(item = subjectsWith(noResult = null), chartActionLabel = "Ver detalle")
		}

		onNodeWithText("47").assertIsDisplayed()
		onNodeWithText("materias cursadas").assertIsDisplayed()
		onNodeWithText("44 aprobadas").assertIsDisplayed()
		onNodeWithText("44 de 46 aprobadas al primer intento").assertIsDisplayed()
		onAllNodesWithText("sin resultado", substring = true).assertCountEquals(0)
	}

	@Test
	fun when_someSubjectsHaveNoResult_then_theirLineIsWrittenWithTheNotes() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsSubjectsView(
				item = subjectsWith(noResult = words("2 materias sin resultado")),
				chartActionLabel = "Ver detalle"
			)
		}

		onNodeWithText("2 materias sin resultado").assertIsDisplayed()
		onNodeWithText("ninguna repetida").assertIsDisplayed()
	}

	@Test
	fun when_aSegmentIsTapped_then_itsTooltipShowsItsCountAndItsShare() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsSubjectsView(item = subjectsWith(noResult = null), chartActionLabel = "Ver detalle")
		}
		waitForIdle()

		onNodeWithTag(SummaryUiTags.statisticsTarget(SummaryStatisticsChartKey.Subjects, 1)).performClick()

		assertTooltipShows("2 reprobadas · 4 %")
	}

	private fun subjectsWith(noResult: UiText?): SummaryStatisticsSubjectsItem {
		return SummaryStatisticsSubjectsItem(
			title = words("Materias"),
			content = SummaryStatisticsSubjectsItem.Content.Data(
				figure = figureOf(value = "47", label = "materias cursadas"),
				segments = listOf(
					segmentOf(SummaryStatisticsOutcome.APPROVED, 44f, "44 aprobadas", "44 aprobadas · 94 %"),
					segmentOf(SummaryStatisticsOutcome.FAILED, 2f, "2 reprobadas", "2 reprobadas · 4 %"),
					segmentOf(SummaryStatisticsOutcome.WITHDRAWN, 1f, "1 retirada", "1 retirada · 2 %")
				),
				notes = listOf(words("44 de 46 aprobadas al primer intento"), words("ninguna repetida")),
				noResult = noResult
			)
		)
	}
}
