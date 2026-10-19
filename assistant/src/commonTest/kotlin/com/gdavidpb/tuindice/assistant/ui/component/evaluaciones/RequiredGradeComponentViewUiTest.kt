package com.gdavidpb.tuindice.assistant.ui.component.evaluaciones

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.presentation.mapper.toRequiredGradeItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.RequiredGradeComponent
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class RequiredGradeComponentViewUiTest {
	@Test
	fun when_theTargetIsReachable_then_thePointsItAsksForAreTheFigure_andTheRowNamesTheSubject() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RequiredGradeComponentView(item = required().toRequiredGradeItem(), onAction = {})
		}

		assertNodeVisible(AssistantUiTags.Evaluations.requiredGrade(COMPONENT_ID))
		onNodeWithText("NOTA META").assertIsDisplayed()
		onNodeWithContentDescription("Puntos que faltan para la meta: 25").assertIsDisplayed()
		onNodeWithContentDescription("Puntos por evaluar: 40").assertIsDisplayed()
		onNodeWithText("IS-101").assertIsDisplayed()
		onNodeWithText("Programación I").assertIsDisplayed()
		onNodeWithText("Meta 15").assertIsDisplayed()
		onNodeWithText("alcanzable").assertIsDisplayed()
	}

	@Test
	fun when_theTargetIsNotReachable_then_thePointsLeftToEvaluateAreTheFigure_andNoRequiredFigureShows() =
		runTuIndiceUiTest {
			setTuIndiceTestContent {
				RequiredGradeComponentView(
					item = required(requiredPoints = null, feasible = false).toRequiredGradeItem(),
					onAction = {}
				)
			}

			onNodeWithContentDescription("Puntos por evaluar: 40").assertIsDisplayed()
			onNodeWithContentDescription("Puntos que faltan para la meta: 25").assertDoesNotExist()
			onNodeWithText("no alcanzable").assertIsDisplayed()
		}

	private fun required(requiredPoints: Double? = 25.0, feasible: Boolean = true): RequiredGradeComponent =
		RequiredGradeComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			subjectCode = "IS-101",
			subjectName = "Programación I",
			targetGrade = 15,
			requiredPoints = requiredPoints,
			remainingPoints = 40.0,
			feasible = feasible
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Nota requerida de prueba"
	}
}
