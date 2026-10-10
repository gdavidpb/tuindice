package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.evaluations.presentation.mapper.buildEvaluationsWeekItems
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationWeekDayItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_TERM
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertTrue

// Being today (the filled circle and its colours) and having evaluations (the dot under the day)
// are only drawn: nothing here reads them.
@OptIn(ExperimentalTestApi::class)
class EvaluationWeekDayViewUiTest {
	@Test
	fun when_dayIsRendered_then_readsItsWeekdayOverItsDayNumber() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationWeekDayView(
				item = EvaluationWeekDayItem(
					weekdayText = UiText.Raw("MIÉ"),
					dayText = "21",
					isSelected = true,
					hasEvaluations = true
				)
			)
		}

		val weekday = onNodeWithText("MIÉ").assertIsDisplayed().getUnclippedBoundsInRoot()
		val day = onNodeWithText("21").assertIsDisplayed().getUnclippedBoundsInRoot()

		assertTrue(weekday.bottom <= day.top, "the weekday goes over the day number")
	}

	@Test
	fun when_daysComeFromTheWeekMapping_then_eachReadsItsShortWeekdayInUpperCase() = runTuIndiceUiTest {
		val week = buildEvaluationsWeekItems(
			currentTerm = DEFAULT_EVALUATION_TERM,
			evaluations = emptyList(),
			weekLabelPattern = "Semana %1${'$'}d",
			continuousLabel = "Continuas",
			currentDate = LocalDate(2026, 5, 21)
		).first { item -> item.key == EvaluationsWeekKey.Academic(8) }

		setTuIndiceTestContent {
			Row(modifier = Modifier.fillMaxWidth()) {
				week.days.forEach { day ->
					EvaluationWeekDayView(
						modifier = Modifier.weight(1f),
						item = day
					)
				}
			}
		}

		// Resources resolve asynchronously off Android: wait for the text, then read the nodes.
		waitUntil(timeoutMillis = RESOURCE_TIMEOUT_MILLIS) {
			onAllNodesWithText("DOM").fetchSemanticsNodes().isNotEmpty()
		}

		listOf("LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM").forEach { weekday ->
			onNodeWithText(weekday).assertIsDisplayed()
		}
		week.days.forEach { day ->
			onNodeWithText(day.dayText).assertIsDisplayed()
		}

		val monday = onNodeWithText("LUN").getUnclippedBoundsInRoot()
		val sunday = onNodeWithText("DOM").getUnclippedBoundsInRoot()

		assertTrue(monday.right <= sunday.left, "the week reads from Monday to Sunday")
	}

	@Test
	fun when_theDayChanges_then_readsTheNewWeekdayAndDayNumber() = runTuIndiceUiTest {
		val itemState = mutableStateOf(
			EvaluationWeekDayItem(
				weekdayText = UiText.Raw("LUN"),
				dayText = "18",
				isSelected = false,
				hasEvaluations = false
			)
		)

		setTuIndiceTestContent {
			EvaluationWeekDayView(item = itemState.value)
		}

		onNodeWithText("LUN").assertIsDisplayed()
		onNodeWithText("18").assertIsDisplayed()

		runOnIdle {
			itemState.value = EvaluationWeekDayItem(
				weekdayText = UiText.Raw("MAR"),
				dayText = "19",
				isSelected = true,
				hasEvaluations = true
			)
		}

		onNodeWithText("MAR").assertIsDisplayed()
		onNodeWithText("19").assertIsDisplayed()
		onNodeWithText("LUN").assertDoesNotExist()
		onNodeWithText("18").assertDoesNotExist()
	}

	private companion object {
		const val RESOURCE_TIMEOUT_MILLIS = 5_000L
	}
}
