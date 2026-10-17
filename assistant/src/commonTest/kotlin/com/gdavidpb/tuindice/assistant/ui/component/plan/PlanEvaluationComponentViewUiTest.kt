package com.gdavidpb.tuindice.assistant.ui.component.plan

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.PlanEvaluationComponent
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PlanEvaluationComponentViewUiTest {
	@Test
	fun when_aPlanIsEvaluated_then_itsBandIsTheFigure_andItsCreditsRequirementsAndWarningsShow() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PlanEvaluationComponentView(component = evaluation(), onAction = {})
		}

		assertNodeVisible(AssistantUiTags.Plan.evaluation(COMPONENT_ID))
		onNodeWithText("2026 ENERO-MARZO").assertIsDisplayed()
		onNodeWithContentDescription("Banda de carga: Normal").assertIsDisplayed()
		onNodeWithContentDescription("Índice de carga: 0,6000").assertIsDisplayed()
		onNodeWithText("Créditos").assertIsDisplayed()
		onNodeWithText("14 UC").assertIsDisplayed()
		onNodeWithText("REQUISITOS PENDIENTES").assertIsDisplayed()
		onNodeWithText("Aprobar Cálculo I").assertIsDisplayed()
		onNodeWithText("AVISOS DE HORARIO").assertIsDisplayed()
		onNodeWithText("Solo hay 2 bloques libres").assertIsDisplayed()
		onNodeWithText("Choques sin revisar").assertIsDisplayed()
	}

	@Test
	fun when_theClashesAreCounted_then_theyShowAsACount_andAMissingBandShowsNoFigure() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PlanEvaluationComponentView(
				component = evaluation(loadBand = "", loadIndex = null, clashes = 2),
				onAction = {}
			)
		}

		onNodeWithText("Choques").assertIsDisplayed()
		onNodeWithText("2").assertIsDisplayed()
		onNodeWithText("Choques sin revisar").assertDoesNotExist()
		onNodeWithContentDescription("Banda de carga: Normal").assertDoesNotExist()
		onNodeWithContentDescription("Índice de carga: 0,6000").assertDoesNotExist()
	}

	private fun evaluation(
		loadBand: String = "Normal",
		loadIndex: Double? = 0.6,
		clashes: Int = 0
	): PlanEvaluationComponent =
		PlanEvaluationComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			termLabel = "2026 enero-marzo",
			credits = 14,
			loadBand = loadBand,
			loadIndex = loadIndex,
			clashes = clashes,
			pendingRequirements = listOf("Aprobar Cálculo I"),
			slotWarnings = listOf("Solo hay 2 bloques libres")
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Evaluación de prueba"
	}
}
