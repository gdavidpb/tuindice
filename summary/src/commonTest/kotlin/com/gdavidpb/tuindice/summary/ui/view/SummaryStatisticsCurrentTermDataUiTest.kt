package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCurrentTermItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsEvaluationTypeItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsCurrentTermDataUiTest {
	@Test
	fun when_theTermHasNoOverdueEvaluation_then_noOverdueLineIsWritten() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			DataHost(overdue = null)
		}
		waitForIdle()

		assertNodeVisible(SummaryUiTags.StatisticsFigure)
		onAllNodesWithTag(SummaryUiTags.StatisticsOverdue).assertCountEquals(0)
	}

	@Test
	fun when_theTermHasOverdueEvaluations_then_theOverdueLineIsWritten() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			DataHost(
				overdue = SummaryStatisticsCurrentTermItem.Overdue(text = words("2 vencidas sin nota"), target = null)
			)
		}
		waitForIdle()

		assertNodeVisible(SummaryUiTags.StatisticsOverdue)
	}

	@Composable
	private fun DataHost(overdue: SummaryStatisticsCurrentTermItem.Overdue?) {
		Column {
			SummaryStatisticsCurrentTermData(
				data = SummaryStatisticsCurrentTermItem.Content.Data(
					figure = figureOf(value = "10", label = "de 400 puntos evaluados", trailing = "3 %"),
					fraction = 0.025f,
					rows = listOf(
						SummaryStatisticsEvaluationTypeItem(
							name = words("Quices"),
							gradedText = words("1 de 1 calificada"),
							scoreText = words("8 de 10 · 80 %"),
							fraction = 0.8f,
							tooltip = null
						)
					),
					overdue = overdue
				),
				chartActionLabel = "Ver detalle",
				actions = SummaryHubActions()
			)
		}
	}
}
