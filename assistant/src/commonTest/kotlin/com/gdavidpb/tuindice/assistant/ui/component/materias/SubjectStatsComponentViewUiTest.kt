package com.gdavidpb.tuindice.assistant.ui.component.materias

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.SubjectStatsComponent
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SubjectStatsComponentViewUiTest {
	@Test
	fun when_theSubjectHasStatistics_then_itsAverageIsTheFigure_andItsDifficultyMedianAndHistogramShow() =
		runTuIndiceUiTest {
			setTuIndiceTestContent {
				SubjectStatsComponentView(component = stats(), onAction = {})
			}

			assertNodeVisible(AssistantUiTags.Subjects.stats(COMPONENT_ID))
			onNodeWithText("PROGRAMACIÓN I").assertIsDisplayed()
			onNodeWithContentDescription("Promedio: 14,50").assertIsDisplayed()
			onNodeWithContentDescription("Tasa de aprobación: 72 %").assertIsDisplayed()
			onNodeWithText("Dificultad").assertIsDisplayed()
			onNodeWithText("Media").assertIsDisplayed()
			onNodeWithText("Mediana").assertIsDisplayed()
			onNodeWithText("15").assertIsDisplayed()
			onNodeWithText("DISTRIBUCIÓN DE NOTAS").assertIsDisplayed()
		}

	@Test
	fun when_theAverageAndTheDifficultyAreMissing_then_thePassRateIsTheFigure_andTheirRowsAreNotDrawn() =
		runTuIndiceUiTest {
			setTuIndiceTestContent {
				SubjectStatsComponentView(
					component = stats(average = null, median = null, difficulty = null),
					onAction = {}
				)
			}

			onNodeWithContentDescription("Tasa de aprobación: 72 %").assertIsDisplayed()
			onNodeWithText("Dificultad").assertDoesNotExist()
			onNodeWithText("Mediana").assertDoesNotExist()
		}

	private fun stats(
		average: Double? = 14.5,
		median: Double? = 15.0,
		difficulty: String? = "medium"
	): SubjectStatsComponent =
		SubjectStatsComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			subjectCode = "IS-101",
			subjectName = "Programación I",
			scope = "all",
			average = average,
			median = median,
			passRate = 0.72,
			difficulty = difficulty,
			histogram = listOf(0, 2, 10, 20, 12)
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Estadísticas de prueba"
	}
}
