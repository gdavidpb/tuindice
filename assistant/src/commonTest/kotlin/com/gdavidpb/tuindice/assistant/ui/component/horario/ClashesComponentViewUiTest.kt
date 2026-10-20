package com.gdavidpb.tuindice.assistant.ui.component.horario

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.presentation.mapper.toClashesItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.ClashesComponent
import com.gdavidpb.tuindice.chatcontract.component.ScheduleClash
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ClashesComponentViewUiTest {
	@Test
	fun when_thereAreClashes_then_eachShowsItsSubjectsDayAndBlock() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ClashesComponentView(item = clashes(listOf(CLASH)).toClashesItem(), rowLimit = null)
		}

		assertNodeVisible(AssistantUiTags.Schedule.clashes(COMPONENT_ID))
		onNodeWithText("IS-101").assertIsDisplayed()
		onNodeWithText("IS-102").assertIsDisplayed()
		onNodeWithText("Miércoles · bloque\u00A03").assertIsDisplayed()
	}

	@Test
	fun when_aClashSpansSeveralBlocks_then_itNamesThemAsAPlural() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ClashesComponentView(
				item = clashes(listOf(CLASH.copy(startBlock = 3, endBlock = 4, endMinute = 690))).toClashesItem(),
				rowLimit = null
			)
		}

		onNodeWithText("Miércoles · bloques\u00A03-4").assertIsDisplayed()
	}

	@Test
	fun when_thereAreNoClashes_then_aLineSaysSo_andNoRowIsShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ClashesComponentView(item = clashes(emptyList()).toClashesItem(), rowLimit = null)
		}

		onNodeWithText("No tienes choques de horario").assertIsDisplayed()
		onNodeWithText("IS-101").assertDoesNotExist()
	}

	private fun clashes(items: List<ScheduleClash>): ClashesComponent =
		ClashesComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			clashes = items
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Choques de prueba"

		// Block 3 alone: 9:30 to 10:30, on Wednesday.
		val CLASH = ScheduleClash(
			dayOfWeek = 3,
			startMinute = 570,
			endMinute = 630,
			startBlock = 3,
			endBlock = 3,
			subjectCodes = listOf("IS-101", "IS-102")
		)
	}
}
