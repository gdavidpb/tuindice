package com.gdavidpb.tuindice.assistant.ui.component.informe

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.presentation.mapper.toIndexGoalItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.IndexGoalComponent
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class IndexGoalComponentViewUiTest {
	@Test
	fun when_theGoalIsFeasible_then_theRequiredIndexIsTheFigure_andTheChipSaysSo() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			IndexGoalComponentView(item = goal(feasible = true).toIndexGoalItem())
		}

		assertNodeVisible(AssistantUiTags.Report.indexGoal(COMPONENT_ID))
		onNodeWithText("META DEL ÍNDICE").assertIsDisplayed()
		onNodeWithContentDescription("Índice necesario en el trimestre: 18.2000").assertIsDisplayed()
		onNodeWithText("Meta acumulada").assertIsDisplayed()
		onNodeWithText("17.0000").assertIsDisplayed()
		onNodeWithText("Alcanzable").assertIsDisplayed()
		onNodeWithText("Créditos").assertIsDisplayed()
		onNodeWithText("60").assertIsDisplayed()
	}

	@Test
	fun when_theGoalIsNotFeasible_then_theChipSaysItIsNot() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			IndexGoalComponentView(item = goal(feasible = false).toIndexGoalItem())
		}

		onNodeWithText("No alcanzable").assertIsDisplayed()
		onNodeWithText("Alcanzable").assertDoesNotExist()
	}

	private fun goal(feasible: Boolean): IndexGoalComponent =
		IndexGoalComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			targetCumulative = 17.0,
			credits = 60,
			requiredTermIndex = 18.2,
			feasible = feasible
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Meta de prueba"
	}
}
