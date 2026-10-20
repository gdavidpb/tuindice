package com.gdavidpb.tuindice.assistant.ui.component.informe

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.presentation.mapper.toIndexSeriesItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.IndexPoint
import com.gdavidpb.tuindice.chatcontract.component.IndexSeriesComponent
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class IndexSeriesComponentViewUiTest {
	@Test
	fun when_aSeriesHasTerms_then_theLatestCumulativeIsTheFigure_andTheExtremesAreMarked() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			IndexSeriesComponentView(item = series().toIndexSeriesItem())
		}

		assertNodeVisible(AssistantUiTags.Report.indexSeries(COMPONENT_ID))
		onNodeWithText("ÍNDICE POR TRIMESTRE").assertIsDisplayed()
		onNodeWithContentDescription("Índice acumulado: 15.8000").assertIsDisplayed()
		onNodeWithText("2025 septiembre-diciembre").assertIsDisplayed()
		onNodeWithText("2026 enero-marzo").assertIsDisplayed()
		onNodeWithText("16.2000").assertIsDisplayed()
		onNodeWithText("15.1000").assertIsDisplayed()
		onNodeWithText("Más alto").assertIsDisplayed()
		onNodeWithText("Más bajo").assertIsDisplayed()
	}

	@Test
	fun when_theSeriesHasNoTerms_then_thereIsNoFigure() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			IndexSeriesComponentView(item = series(points = emptyList()).toIndexSeriesItem())
		}

		onNodeWithText("ÍNDICE POR TRIMESTRE").assertIsDisplayed()
		onNodeWithContentDescription("Índice acumulado: 15.8000").assertDoesNotExist()
	}

	private fun series(points: List<IndexPoint> = POINTS): IndexSeriesComponent =
		IndexSeriesComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			points = points,
			bestTermId = "2026-JAN_MAR",
			worstTermId = "2025-SEP_DEC"
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Serie de prueba"

		val POINTS = listOf(
			IndexPoint("2025-SEP_DEC", "2025 septiembre-diciembre", null, 15.1),
			IndexPoint("2026-JAN_MAR", "2026 enero-marzo", 16.2, 15.8)
		)
	}
}
