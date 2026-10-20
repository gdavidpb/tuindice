package com.gdavidpb.tuindice.assistant.ui.component.horario

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.presentation.mapper.toNextClassItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.NextClassComponent
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

// The class starts START_MINUTE minutes after the start of its day, and the screen reads the clock it is given.
@OptIn(ExperimentalTestApi::class)
class NextClassComponentViewUiTest {
	@Test
	fun when_theClassIsLaterToday_then_itsHours_subjectRoom_andWhatIsLeftAreShown() = runTuIndiceUiTest {
		showNextClass(nextClass(), nowMillis = DAY_START_MILLIS + (START_MINUTE - 30) * MILLIS_PER_MINUTE)

		assertNodeVisible(AssistantUiTags.Schedule.nextClass(COMPONENT_ID))
		onNodeWithText("PRÓXIMA CLASE").assertIsDisplayed()
		onNodeWithContentDescription("Hora de la clase: 8:00–10:00 a. m.").assertIsDisplayed()
		onNodeWithText("IS-101").assertIsDisplayed()
		onNodeWithText("Programación I").assertIsDisplayed()
		onNodeWithText("A-1").assertIsDisplayed()
		onNodeWithText("Cuánto falta").assertIsDisplayed()
		onNodeWithText("30 min").assertIsDisplayed()
	}

	@Test
	fun when_theClassIsInProgress_then_itSaysSo_andNothingIsLeft() = runTuIndiceUiTest {
		showNextClass(
			nextClass(inProgress = true),
			nowMillis = DAY_START_MILLIS + (START_MINUTE + 10) * MILLIS_PER_MINUTE
		)

		onNodeWithText("CLASE EN CURSO").assertIsDisplayed()
		onNodeWithText("En curso").assertIsDisplayed()
		onNodeWithText("Cuánto falta").assertDoesNotExist()
	}

	@Test
	fun when_theClassIsOverAndNotInProgress_then_nothingCountsDown() = runTuIndiceUiTest {
		showNextClass(
			nextClass(),
			nowMillis = DAY_START_MILLIS + (START_MINUTE + 3 * MINUTES_PER_HOUR) * MILLIS_PER_MINUTE
		)

		onNodeWithText("Cuánto falta").assertDoesNotExist()
	}

	@Test
	fun when_theClassIsDaysAway_then_itCountsDays() = runTuIndiceUiTest {
		showNextClass(
			nextClass(),
			nowMillis = DAY_START_MILLIS + START_MINUTE * MILLIS_PER_MINUTE - 3 * MINUTES_PER_DAY * MILLIS_PER_MINUTE
		)

		onNodeWithText("3 días").assertIsDisplayed()
	}

	private fun ComposeUiTest.showNextClass(component: NextClassComponent, nowMillis: Long) {
		setTuIndiceTestContent {
			NextClassComponentView(item = component.toNextClassItem(nowMillis = nowMillis))
		}
	}

	private fun nextClass(inProgress: Boolean = false): NextClassComponent =
		NextClassComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			subjectCode = "IS-101",
			subjectName = "Programación I",
			dayOfWeek = 2,
			startMinute = START_MINUTE,
			endMinute = START_MINUTE + 120,
			room = "A-1",
			inProgress = inProgress,
			dateMillis = DAY_START_MILLIS
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Próxima clase de prueba"
		const val DAY_START_MILLIS = 1_760_000_000_000L
		const val START_MINUTE = 480
		const val MILLIS_PER_MINUTE = 60_000L
		const val MINUTES_PER_HOUR = 60
		const val MINUTES_PER_DAY = 1_440
	}
}
