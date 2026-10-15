package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsBarItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFactItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsGradesItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsGradesDataUiTest {
	@Test
	fun when_gradesAreGiven_then_theirHistogramIsWritten() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			DataHost(facts = emptyList())
		}
		waitForIdle()

		assertNodeVisible(SummaryUiTags.statisticsChart(SummaryStatisticsChartKey.Grades))
	}

	@Test
	fun when_aFactIsGiven_then_ItIsWrittenBelowTheHistogram() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			DataHost(facts = listOf(SummaryStatisticsFactItem(value = "3", label = words("sin reportar"))))
		}
		waitForIdle()

		onNodeWithText("sin reportar").assertIsDisplayed()
	}

	@Composable
	private fun DataHost(facts: List<SummaryStatisticsFactItem>) {
		Column {
			SummaryStatisticsGradesData(
				data = SummaryStatisticsGradesItem.Content.Data(
					bars = listOf(
						SummaryStatisticsBarItem(
							axisLabel = words("5"),
							value = 3f,
							tooltip = tooltipOf("Nota 5: 3 materias"),
							valueLabel = words("3")
						)
					),
					facts = facts
				),
				summary = "Tus notas",
				chartActionLabel = "Ver detalle"
			)
		}
	}
}
