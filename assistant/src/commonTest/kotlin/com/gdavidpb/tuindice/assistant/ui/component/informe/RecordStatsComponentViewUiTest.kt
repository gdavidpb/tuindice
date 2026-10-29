package com.gdavidpb.tuindice.assistant.ui.component.informe

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.presentation.mapper.toRecordStatsItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.RecordStatsComponent
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class RecordStatsComponentViewUiTest {
	@Test
	fun when_theRecordHasCounts_then_theApprovedIsTheFigure_andTheRestAreRows() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordStatsComponentView(item = stats().toRecordStatsItem())
		}

		assertNodeVisible(AssistantUiTags.Report.recordStats(COMPONENT_ID))
		onNodeWithContentDescription("Materias aprobadas: 44").assertIsDisplayed()
		onNodeWithText("Reprobadas").assertIsDisplayed()
		onNodeWithText("9").assertIsDisplayed()
		onNodeWithText("Retiradas").assertIsDisplayed()
		onNodeWithText("6").assertIsDisplayed()
		onNodeWithText("Aprobadas al primer intento").assertIsDisplayed()
		onNodeWithText("40").assertIsDisplayed()
		onNodeWithText("Repetidas").assertIsDisplayed()
		onNodeWithText("8").assertIsDisplayed()
		onNodeWithText("Distribución de notas").assertIsDisplayed()
	}

	@Test
	fun when_theHistogramHasNoGrades_then_NoBarsAreShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordStatsComponentView(item = stats(histogram = emptyList()).toRecordStatsItem())
		}

		onNodeWithText("Distribución de notas").assertDoesNotExist()
		onNodeWithText("Reprobadas").assertIsDisplayed()
	}

	private fun stats(histogram: List<Int> = HISTOGRAM): RecordStatsComponent =
		RecordStatsComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			approved = 44,
			failed = 9,
			retired = 6,
			approvedFirstTry = 40,
			repeated = 8,
			histogram = histogram
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Estadísticas de prueba"
		val HISTOGRAM = listOf(0, 2, 10, 20, 12)
	}
}
