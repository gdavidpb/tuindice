package com.gdavidpb.tuindice.assistant.ui.component.informe

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.presentation.mapper.toSimulationItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.SimulatedGrade
import com.gdavidpb.tuindice.chatcontract.component.SimulationComponent
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SimulationComponentViewUiTest {
	@Test
	fun when_aTermIsSimulated_then_itsIndexesAreTheFigures_andEachGradeIsARow() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SimulationComponentView(item = simulation().toSimulationItem())
		}

		assertNodeVisible(AssistantUiTags.Report.simulation(COMPONENT_ID))
		onNodeWithText("SIMULACIÓN DEL TRIMESTRE").assertIsDisplayed()
		onNodeWithContentDescription("Índice del trimestre simulado: 17.0000").assertIsDisplayed()
		onNodeWithContentDescription("Acumulado simulado: 16.0000").assertIsDisplayed()
		onNodeWithText("IS-101").assertIsDisplayed()
		onNodeWithText("Aprobada").assertIsDisplayed()
		onNodeWithText("20").assertIsDisplayed()
	}

	@Test
	fun when_theResultIsUnknown_then_theRowShowsNoResult() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SimulationComponentView(item = simulation(outcome = "unreported").toSimulationItem())
		}

		onNodeWithText("IS-101").assertIsDisplayed()
		onNodeWithText("Aprobada").assertDoesNotExist()
	}

	private fun simulation(outcome: String? = "approved"): SimulationComponent =
		SimulationComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			termId = "2026-JAN_MAR",
			termIndex = 17.0,
			cumulativeIndex = 16.0,
			changes = listOf(SimulatedGrade("attempt-1", "IS-101", grade = 20, outcome = outcome))
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Simulación de prueba"
	}
}
