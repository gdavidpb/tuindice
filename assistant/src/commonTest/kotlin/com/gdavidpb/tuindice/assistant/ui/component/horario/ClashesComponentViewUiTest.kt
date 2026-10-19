package com.gdavidpb.tuindice.assistant.ui.component.horario

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
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
	fun when_thereAreClashes_then_eachShowsItsSubjectsDayAndHours() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ClashesComponentView(item = clashes(listOf(CLASH)).toClashesItem())
		}

		assertNodeVisible(AssistantUiTags.Schedule.clashes(COMPONENT_ID))
		onNodeWithContentDescription("Choques de horario: 1").assertIsDisplayed()
		onNodeWithText("IS-101 · IS-102").assertIsDisplayed()
		onNodeWithText("Miércoles · 9:00–10:00 a. m.").assertIsDisplayed()
	}

	@Test
	fun when_thereAreNoClashes_then_noRowIsShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ClashesComponentView(item = clashes(emptyList()).toClashesItem())
		}

		onNodeWithContentDescription("Choques de horario: 0").assertIsDisplayed()
		onNodeWithText("IS-101 · IS-102").assertDoesNotExist()
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
		val CLASH = ScheduleClash(
			dayOfWeek = 3,
			startMinute = 540,
			endMinute = 600,
			subjectCodes = listOf("IS-101", "IS-102")
		)
	}
}
