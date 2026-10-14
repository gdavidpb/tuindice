package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsBarItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsGradesItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsLabelTags
import com.gdavidpb.tuindice.summary.ui.chart.assertTooltipShows
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsGradesViewUiTest {
	@Test
	fun when_gradesAreCounted_then_eachBarWritesItsCountAboveItAndItsGradeBelow() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsGradesView(item = gradesWithCounts(), chartActionLabel = "Ver detalle")
		}
		waitForIdle()

		onNodeWithTag(
			testTag = StatisticsLabelTags.value(SummaryStatisticsChartKey.Grades, 3),
			useUnmergedTree = true
		).assertTextEquals("21")
		onNodeWithTag(
			testTag = StatisticsLabelTags.axis(SummaryStatisticsChartKey.Grades, 3),
			useUnmergedTree = true
		).assertTextEquals("4")
		onNodeWithText("La más frecuente: 4.").assertIsDisplayed()
		onNodeWithText("3 sin reportar").assertIsDisplayed()
	}

	@Test
	fun when_aBarIsTapped_then_itsTooltipShowsTheGradeAndItsCount() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsGradesView(item = gradesWithCounts(), chartActionLabel = "Ver detalle")
		}
		waitForIdle()

		onNodeWithTag(SummaryUiTags.statisticsTarget(SummaryStatisticsChartKey.Grades, 3)).performClick()

		assertTooltipShows("Nota 4: 21 materias")
	}

	private fun gradesWithCounts(): SummaryStatisticsGradesItem {
		return SummaryStatisticsGradesItem(
			title = words("Tus notas"),
			content = SummaryStatisticsGradesItem.Content.Data(
				bars = listOf(
					bar(grade = "1", count = "0", title = "Nota 1: 0 materias"),
					bar(grade = "2", count = "2", title = "Nota 2: 2 materias"),
					bar(grade = "3", count = "7", title = "Nota 3: 7 materias"),
					bar(grade = "4", count = "21", title = "Nota 4: 21 materias"),
					bar(grade = "5", count = "16", title = "Nota 5: 16 materias")
				),
				notes = listOf(
					words("Una nota por materia cursada, repetidas incluidas."),
					words("La más frecuente: 4.")
				),
				unreported = words("3 sin reportar")
			)
		)
	}

	private fun bar(grade: String, count: String, title: String): SummaryStatisticsBarItem {
		return SummaryStatisticsBarItem(
			axisLabel = words(grade),
			value = count.toFloat(),
			tooltip = tooltipOf(title, "46 % de tus 46 notas"),
			valueLabel = words(count)
		)
	}
}
