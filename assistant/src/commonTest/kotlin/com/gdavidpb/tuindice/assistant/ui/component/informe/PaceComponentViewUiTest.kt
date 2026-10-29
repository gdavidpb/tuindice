package com.gdavidpb.tuindice.assistant.ui.component.informe

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.presentation.mapper.toPaceItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.PaceComponent
import com.gdavidpb.tuindice.chatcontract.component.TermCredits
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PaceComponentViewUiTest {
	@Test
	fun when_thePaceHasTerms_then_theAverageIsTheFigure_andEachTermShowsItsCredits() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PaceComponentView(item = pace().toPaceItem())
		}

		assertNodeVisible(AssistantUiTags.Report.pace(COMPONENT_ID))
		onNodeWithText("RITMO DE CRÉDITOS").assertIsDisplayed()
		onNodeWithContentDescription("UC por trimestre: 15.0").assertIsDisplayed()
		onNodeWithText("2025 septiembre-diciembre").assertIsDisplayed()
		onNodeWithText("14\u00A0UC").assertIsDisplayed()
		onNodeWithText("2026 enero-marzo").assertIsDisplayed()
		onNodeWithText("16\u00A0UC").assertIsDisplayed()
		onNodeWithText("Trimestres restantes estimados").assertIsDisplayed()
		onNodeWithText("4").assertIsDisplayed()
	}

	@Test
	fun when_theTermsLeftAreNotEstimated_then_NoRowShowsThem() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PaceComponentView(item = pace(remainingTermsEstimate = null).toPaceItem())
		}

		onNodeWithText("Trimestres restantes estimados").assertDoesNotExist()
	}

	@Test
	fun when_thereIsNoTerm_then_aLineSaysSo() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PaceComponentView(
				item = PaceComponent(
					id = COMPONENT_ID,
					fallbackText = FALLBACK_TEXT,
					creditsPerTerm = emptyList(),
					averageCredits = 0.0
				).toPaceItem()
			)
		}

		onNodeWithText("No hay trimestres para mostrar").assertIsDisplayed()
	}

	private fun pace(remainingTermsEstimate: Int? = 4): PaceComponent =
		PaceComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			creditsPerTerm = listOf(
				TermCredits("2025 septiembre-diciembre", 14),
				TermCredits("2026 enero-marzo", 16)
			),
			averageCredits = 15.0,
			remainingTermsEstimate = remainingTermsEstimate
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Ritmo de prueba"
	}
}
