package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsBarItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsPaceItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsReferenceItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsPaceDataUiTest {
	@Test
	fun when_theTermsArePaced_then_theFigureAndTheChartAreWritten() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			DataHost(projection = SummaryStatisticsPaceItem.Projection.None)
		}
		waitForIdle()

		assertNodeVisible(SummaryUiTags.StatisticsFigure)
		assertNodeVisible(SummaryUiTags.statisticsChart(SummaryStatisticsChartKey.Pace))
	}

	@Test
	fun when_theProjectionIsPending_then_ItIsWrittenUnderTheChart() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			DataHost(projection = SummaryStatisticsPaceItem.Projection.Pending(text = words("Faltan dos trimestres")))
		}
		waitForIdle()

		onNodeWithText("Faltan dos trimestres").assertIsDisplayed()
	}

	@Composable
	private fun DataHost(projection: SummaryStatisticsPaceItem.Projection) {
		Column {
			SummaryStatisticsPaceData(
				data = SummaryStatisticsPaceItem.Content.Data(
					figure = figureOf(value = "13.8", label = "UC aprobadas por trimestre"),
					bars = listOf(
						SummaryStatisticsBarItem(
							axisLabel = words("S-D 23"),
							value = 16f,
							tooltip = tooltipOf("Sep - Dic 2023: 16 UC")
						)
					),
					reference = SummaryStatisticsReferenceItem(value = 13.8f, label = words("promedio 13.8")),
					projection = projection
				),
				summary = "Ritmo",
				chartActionLabel = "Ver detalle"
			)
		}
	}
}
