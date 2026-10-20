package com.gdavidpb.tuindice.assistant.ui.component.evaluaciones

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.presentation.mapper.toWeeklyLoadItem
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
			WeeklyLoadComponentView(item = load().toWeeklyLoadItem(), onAction = {})
		}

		assertNodeVisible(AssistantUiTags.Evaluations.load(COMPONENT_ID))
		onNodeWithContentDescription("Semanas pesadas: 1").assertIsDisplayed()
		onNodeWithText("Semana\u00A042").assertIsDisplayed()
		onNodeWithText("3\u00A0evaluaciones · 60\u00A0puntos").assertIsDisplayed()
		onNodeWithText("Semana\u00A043").assertIsDisplayed()
		onNodeWithText("1\u00A0evaluación · 10\u00A0puntos").assertIsDisplayed()
		onNodeWithText("Pesada").assertIsDisplayed()
	}

	@Test
	fun when_noWeekIsHeavy_then_theFigureIsZero_andNoWeekHasTheChip() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WeeklyLoadComponentView(item = load(heavy = false).toWeeklyLoadItem(), onAction = {})
		}

		onNodeWithContentDescription("Semanas pesadas: 0").assertIsDisplayed()
		onNodeWithText("Pesada").assertDoesNotExist()
	}

	@Test
	fun when_thereIsNoWeek_then_aLineSaysSo() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WeeklyLoadComponentView(
				item = WeeklyLoadComponent(id = COMPONENT_ID, fallbackText = FALLBACK_TEXT, weeks = emptyList())
					.toWeeklyLoadItem(),
				onAction = {}
			)
		}

		onNodeWithText("No hay semanas para mostrar").assertIsDisplayed()
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
