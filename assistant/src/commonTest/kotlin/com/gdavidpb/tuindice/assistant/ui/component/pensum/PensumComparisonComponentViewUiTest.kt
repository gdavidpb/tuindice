package com.gdavidpb.tuindice.assistant.ui.component.pensum

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.presentation.mapper.toPensumComparisonItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.PensumComparisonComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectRef
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PensumComparisonComponentViewUiTest {
	@Test
	fun when_twoPensumsAreCompared_then_theSharedCreditsAreTheFigure_andTheRowsAreWhatTheServiceBrings() =
		runTuIndiceUiTest {
			setTuIndiceTestContent {
				PensumComparisonComponentView(item = comparison().toPensumComparisonItem(), onAction = {})
			}

			assertNodeVisible(AssistantUiTags.Pensum.comparison(COMPONENT_ID))
			onNodeWithText("COMPARACIÓN DEL PENSUM").assertIsDisplayed()
			onNodeWithContentDescription("Créditos en común: 96").assertIsDisplayed()
			onNodeWithText("Carrera").assertIsDisplayed()
			onNodeWithText("Ingeniería Industrial").assertIsDisplayed()
			onNodeWithText("Pensum").assertIsDisplayed()
			onNodeWithText("2016").assertIsDisplayed()
			onNodeWithText("Materias que te faltan").assertIsDisplayed()
			onNodeWithText("IS-401").assertIsDisplayed()
			onNodeWithText("Redes").assertIsDisplayed()
			onNodeWithText("La materia de laboratorio no cuenta.").assertIsDisplayed()
		}

	@Test
	fun when_noSubjectIsMissing_then_itsHeaderIsNotDrawn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumComparisonComponentView(
				item = comparison(missing = emptyList(), notes = emptyList()).toPensumComparisonItem(),
				onAction = {}
			)
		}

		onNodeWithText("Materias que te faltan").assertDoesNotExist()
	}

	private fun comparison(
		missing: List<SubjectRef> = listOf(SubjectRef("IS-401", "Redes")),
		notes: List<String> = listOf("La materia de laboratorio no cuenta.")
	): PensumComparisonComponent =
		PensumComparisonComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			otherCareerName = "Ingeniería Industrial",
			otherPensumLabel = "2016",
			sharedCredits = 96,
			missingSubjects = missing,
			notes = notes
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Comparación de prueba"
	}
}
