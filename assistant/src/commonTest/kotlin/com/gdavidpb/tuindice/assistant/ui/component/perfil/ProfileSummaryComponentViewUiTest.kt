package com.gdavidpb.tuindice.assistant.ui.component.perfil

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.presentation.mapper.toProfileSummaryItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.ProfileSummaryComponent
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ProfileSummaryComponentViewUiTest {
	@Test
	fun when_theProfileHasAnIndex_then_itIsTheFigureAndTheCreditsAreBesideIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ProfileSummaryComponentView(item = profile(cumulativeIndex = 16.4, totalCredits = 180).toProfileSummaryItem())
		}

		assertNodeVisible(AssistantUiTags.Profile.summary(COMPONENT_ID))
		onNodeWithText("TU PERFIL").assertIsDisplayed()
		onNodeWithContentDescription("Índice acumulado: 16.4000").assertIsDisplayed()
		onNodeWithContentDescription("Créditos aprobados: 80\u00A0/\u00A0180").assertIsDisplayed()
		// The career shows by its name only: its code tells the student nothing.
		onNodeWithText("IS").assertDoesNotExist()
		onNodeWithText("Carrera").assertIsDisplayed()
		onNodeWithText("Ingeniería de Sistemas").assertIsDisplayed()
		onNodeWithText("2016").assertIsDisplayed()
		onNodeWithText("2026-JAN_MAR").assertIsDisplayed()
	}

	@Test
	fun when_theIndexIsMissing_then_theCreditsAreTheFigureAndNothingIsInvented() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ProfileSummaryComponentView(
				item = profile(cumulativeIndex = null, totalCredits = null, currentTermLabel = null).toProfileSummaryItem()
			)
		}

		onNodeWithContentDescription("Índice acumulado: 16.4000").assertDoesNotExist()
		onNodeWithContentDescription("Créditos aprobados: 80").assertIsDisplayed()
		onNodeWithText("Trimestre en curso").assertDoesNotExist()
	}

	private fun profile(
		cumulativeIndex: Double? = 16.4,
		totalCredits: Int? = 180,
		currentTermLabel: String? = "2026-JAN_MAR"
	): ProfileSummaryComponent =
		ProfileSummaryComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			careerCode = "IS",
			careerName = "Ingeniería de Sistemas",
			pensumLabel = "2016",
			cumulativeIndex = cumulativeIndex,
			approvedCredits = 80,
			totalCredits = totalCredits,
			currentTermLabel = currentTermLabel
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Perfil de prueba"
	}
}
