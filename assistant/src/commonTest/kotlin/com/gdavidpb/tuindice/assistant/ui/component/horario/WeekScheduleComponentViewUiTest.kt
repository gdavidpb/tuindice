package com.gdavidpb.tuindice.assistant.ui.component.horario

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.presentation.mapper.toWeekScheduleItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.ScheduleBlock
import com.gdavidpb.tuindice.chatcontract.component.ScheduleDay
import com.gdavidpb.tuindice.chatcontract.component.WeekScheduleComponent
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class WeekScheduleComponentViewUiTest {
	@Test
	fun when_theWeekHasClasses_then_eachDayShowsItsBlocksWithCodeNameRoomAndHours() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WeekScheduleComponentView(item = week().toWeekScheduleItem())
		}

		assertNodeVisible(AssistantUiTags.Schedule.week(COMPONENT_ID))
		onNodeWithText("Lunes").assertIsDisplayed()
		onNodeWithText("Miércoles").assertIsDisplayed()
		onNodeWithText("IS-101").assertIsDisplayed()
		onNodeWithText("Programación I · A-1").assertIsDisplayed()
		onNodeWithText("Matemática I").assertIsDisplayed()
		onNodeWithText("1-2 · 7:30–9:30 a. m.").assertIsDisplayed()
		onNodeWithText("Evaluación").assertIsDisplayed()
		onNodeWithText("IS-102").assertIsDisplayed()
		onNodeWithText("5-6 · 11:30 a. m.–1:30 p. m.").assertIsDisplayed()
	}

	@Test
	fun when_aBlockHasHours_then_theHoursAreTheMutedEndOfTheBlock() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WeekScheduleComponentView(item = week().toWeekScheduleItem())
		}
		waitForIdle()

		val schedule = onNodeWithText("1-2 · 7:30–9:30 a. m.").fetchSemanticsNode().config[SemanticsProperties.Text].single()

		// The muted span starts where the hours do, so the hours are the suffix the card mutes: the block stays in full tone.
		assertEquals("1-2 · ".length, schedule.spanStyles.single().start)
	}

	@Test
	fun when_aDayHasNoClasses_then_itIsLeftOut_andAWeekWithNoClassesSaysSo() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WeekScheduleComponentView(item = week(days = listOf(ScheduleDay(2, emptyList()))).toWeekScheduleItem())
		}

		onNodeWithText("Sin clases esta semana").assertIsDisplayed()
		onNodeWithText("Martes").assertDoesNotExist()
	}

	private fun week(days: List<ScheduleDay> = DAYS): WeekScheduleComponent =
		WeekScheduleComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			days = days
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Horario de prueba"

		val DAYS = listOf(
			ScheduleDay(
				1,
				listOf(
					ScheduleBlock(
						subjectCode = "IS-101",
						subjectName = "Programación I",
						startMinute = 450,
						endMinute = 570,
						startBlock = 1,
						endBlock = 2,
						room = "A-1",
						evaluationTitles = listOf("Parcial 1")
					)
				)
			),
			ScheduleDay(
				3,
				listOf(
					ScheduleBlock(
						subjectCode = "IS-102",
						subjectName = "Matemática I",
						startMinute = 690,
						endMinute = 810,
						startBlock = 5,
						endBlock = 6
					)
				)
			)
		)
	}
}
