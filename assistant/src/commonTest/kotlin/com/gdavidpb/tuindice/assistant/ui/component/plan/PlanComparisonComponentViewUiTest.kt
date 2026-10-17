package com.gdavidpb.tuindice.assistant.ui.component.plan

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.PlanComparisonComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanOption
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PlanComparisonComponentViewUiTest {
	@Test
	fun when_plansAreCompared_then_eachBlockReadsItsLoadCreditsAndUnlocks() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PlanComparisonComponentView(component = comparison(), onAction = {})
		}

		assertNodeVisible(AssistantUiTags.Plan.comparison(COMPONENT_ID))
		onNodeWithText("COMPARACIÓN DE PLANES").assertIsDisplayed()
		onNodeWithText("Plan A").assertIsDisplayed()
		onNodeWithText("Media · 14 UC").assertIsDisplayed()
		onNodeWithText("Desbloquea IS-201").assertIsDisplayed()
		onNodeWithText("Plan B").assertIsDisplayed()
		onNodeWithText("Alta · 18 UC").assertIsDisplayed()
	}

	@Test
	fun when_aPlanHasNoKnownLoad_then_itReadsItsCreditsAlone_andWithoutUnlocksNoLineIsDrawn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PlanComparisonComponentView(
				component = comparison(options = listOf(PlanOption("Plan C", 6, "unknown", clashes = 0))),
				onAction = {}
			)
		}

		onNodeWithText("Plan C").assertIsDisplayed()
		onNodeWithText("6 UC").assertIsDisplayed()
		onNodeWithText("Desbloquea", substring = true).assertDoesNotExist()
	}

	private fun comparison(
		options: List<PlanOption> = listOf(
			PlanOption("Plan A", 14, "medium", unlocks = listOf("IS-201"), clashes = 0),
			PlanOption("Plan B", 18, "high", clashes = 1)
		)
	): PlanComparisonComponent =
		PlanComparisonComponent(id = COMPONENT_ID, fallbackText = FALLBACK_TEXT, options = options)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Comparación de planes de prueba"
	}
}
