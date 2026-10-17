package com.gdavidpb.tuindice.assistant.ui.component.evaluaciones

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.WeekLoad
import com.gdavidpb.tuindice.chatcontract.component.WeeklyLoadComponent
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class WeeklyLoadComponentViewUiTest {
	@Test
	fun when_someWeeksAreHeavy_then_theirCountIsTheFigure_andEachWeekReadsItsEvaluationsAndPoints() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WeeklyLoadComponentView(component = load(), onAction = {})
		}

		assertNodeVisible(AssistantUiTags.Evaluations.load(COMPONENT_ID))
		onNodeWithContentDescription("Semanas pesadas: 1").assertIsDisplayed()
		onNodeWithText("Semana 42").assertIsDisplayed()
		onNodeWithText("3 evaluaciones · 60 puntos").assertIsDisplayed()
		onNodeWithText("Semana 43").assertIsDisplayed()
		onNodeWithText("1 evaluación · 10 puntos").assertIsDisplayed()
		onNodeWithText("pesada").assertIsDisplayed()
	}

	@Test
	fun when_noWeekIsHeavy_then_theFigureIsZero_andNoWeekHasTheChip() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WeeklyLoadComponentView(component = load(heavy = false), onAction = {})
		}

		onNodeWithContentDescription("Semanas pesadas: 0").assertIsDisplayed()
		onNodeWithText("pesada").assertDoesNotExist()
	}

	private fun load(heavy: Boolean = true): WeeklyLoadComponent =
		WeeklyLoadComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			weeks = listOf(
				WeekLoad(week = 42, count = 3, points = 60.0, heavy = heavy),
				WeekLoad(week = 43, count = 1, points = 10.0, heavy = false)
			)
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Carga de prueba"
	}
}
