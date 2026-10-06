package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationWeekDayItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// The scale and the fade of the weeks beside the selected one are only drawn: nothing here reads them.
@OptIn(ExperimentalTestApi::class)
class EvaluationsWeekStripViewUiTest {
	@Test
	fun when_rendered_then_theSelectedWeekIsMarked_andItsDaysAreOnThePage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationsWeekStripView(
				items = academicWeeks(),
				selectedWeekKey = EvaluationsWeekKey.Academic(8),
				onWeekSelected = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationsWeekStrip)
		assertNodeVisible(EvaluationsUiTags.evaluationsWeekChip(8))
		assertNodeVisible(EvaluationsUiTags.evaluationsWeekPage(8))

		onNodeWithTag(EvaluationsUiTags.evaluationsWeekChip(8))
			.assertTextEquals("Semana 8")
			.assert(isSelected(true))
		onNodeWithTag(EvaluationsUiTags.evaluationsWeekChip(9)).assert(isSelected(false))

		// Only the days of the selected week are on the page.
		onNodeWithText("LUN").assertIsDisplayed()
		onNodeWithText("18").assertIsDisplayed()
		onNodeWithText("24").assertIsDisplayed()
		onNodeWithText("11").assertDoesNotExist()
		onNodeWithText("25").assertDoesNotExist()
	}

	@Test
	fun when_anotherWeekIsTapped_then_reportsThatWeek() = runTuIndiceUiTest {
		val reportedWeeks = mutableListOf<EvaluationsWeekKey>()

		setTuIndiceTestContent {
			EvaluationsWeekStripView(
				items = academicWeeks(),
				selectedWeekKey = EvaluationsWeekKey.Academic(8),
				onWeekSelected = { weekKey -> reportedWeeks += weekKey }
			)
		}

		assertNodeVisible(EvaluationsUiTags.evaluationsWeekChip(9))
		onNodeWithTag(EvaluationsUiTags.evaluationsWeekChip(9)).performClick()

		assertEquals(listOf<EvaluationsWeekKey>(EvaluationsWeekKey.Academic(9)), reportedWeeks)
	}

	@Test
	fun when_theSelectedWeekChanges_then_thePageMovesToThatWeek() = runTuIndiceUiTest {
		val selectedWeekState = mutableStateOf<EvaluationsWeekKey>(EvaluationsWeekKey.Academic(8))

		setTuIndiceTestContent {
			EvaluationsWeekStripView(
				items = academicWeeks(),
				selectedWeekKey = selectedWeekState.value,
				onWeekSelected = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.evaluationsWeekPage(8))

		runOnIdle {
			selectedWeekState.value = EvaluationsWeekKey.Academic(9)
		}

		assertNodeVisible(EvaluationsUiTags.evaluationsWeekPage(9))
		waitUntil(timeoutMillis = SETTLE_TIMEOUT_MILLIS) {
			onNodeWithTag(EvaluationsUiTags.evaluationsWeekChip(9))
				.fetchSemanticsNode()
				.config
				.getOrNull(SemanticsProperties.Selected) == true
		}

		onNodeWithText("25").assertIsDisplayed()
		onNodeWithTag(EvaluationsUiTags.evaluationsWeekChip(8)).assert(isSelected(false))
	}

	@Test
	fun when_thePageIsSwiped_then_reportsTheNextWeek() = runTuIndiceUiTest {
		val reportedWeeks = mutableListOf<EvaluationsWeekKey>()

		setTuIndiceTestContent {
			EvaluationsWeekStripView(
				items = academicWeeks(),
				selectedWeekKey = EvaluationsWeekKey.Academic(8),
				onWeekSelected = { weekKey -> reportedWeeks += weekKey }
			)
		}

		assertNodeVisible(EvaluationsUiTags.evaluationsWeekPage(8))
		onNodeWithTag(EvaluationsUiTags.evaluationsWeekPage(8)).performTouchInput { swipeLeft() }

		waitUntil(timeoutMillis = SETTLE_TIMEOUT_MILLIS) { reportedWeeks.isNotEmpty() }

		assertEquals(EvaluationsWeekKey.Academic(9), reportedWeeks.last())
	}

	@Test
	fun when_weeksArriveOutOfOrder_then_theyAreLaidOutByTheirKey_continuousFirst() = runTuIndiceUiTest {
		val continuous = EvaluationsWeekItem(
			key = EvaluationsWeekKey.Continuous,
			labelText = "Continuas",
			days = emptyList(),
			isCurrent = false
		)

		setTuIndiceTestContent {
			EvaluationsWeekStripView(
				items = academicWeeks().reversed() + continuous,
				selectedWeekKey = EvaluationsWeekKey.Continuous,
				onWeekSelected = {}
			)
		}

		val continuousChipTag = EvaluationsUiTags.evaluationsWeekChip(EvaluationsWeekKey.Continuous)

		assertNodeVisible(continuousChipTag)
		assertNodeVisible(EvaluationsUiTags.evaluationsWeekChip(7))
		// The continuous page is there, but with no days it takes no height.
		onNodeWithTag(EvaluationsUiTags.evaluationsWeekPage(EvaluationsWeekKey.Continuous)).assertExists()

		onNodeWithTag(continuousChipTag)
			.assertTextEquals("Continuas")
			.assert(isSelected(true))

		val continuousChip = onNodeWithTag(continuousChipTag).getUnclippedBoundsInRoot()
		val firstWeekChip = onNodeWithTag(EvaluationsUiTags.evaluationsWeekChip(7)).getUnclippedBoundsInRoot()

		assertTrue(continuousChip.right <= firstWeekChip.left, "continuous goes before the first week")
		onNodeWithText("LUN").assertDoesNotExist()
	}

	private fun isSelected(selected: Boolean) =
		SemanticsMatcher.expectValue(SemanticsProperties.Selected, selected)

	private fun academicWeeks(): List<EvaluationsWeekItem> = listOf(7, 8, 9).map { weekNumber ->
		val firstDay = FIRST_DAY_OF_WEEK_SEVEN + (weekNumber - FIRST_WEEK) * WEEKDAYS.size

		EvaluationsWeekItem(
			key = EvaluationsWeekKey.Academic(weekNumber),
			labelText = "Semana $weekNumber",
			days = WEEKDAYS.mapIndexed { offset, weekday ->
				EvaluationWeekDayItem(
					weekdayText = UiText.Raw(weekday),
					dayText = (firstDay + offset).toString(),
					isSelected = false,
					hasEvaluations = false
				)
			},
			isCurrent = false
		)
	}

	private companion object {
		const val SETTLE_TIMEOUT_MILLIS = 5_000L
		const val FIRST_WEEK = 7
		const val FIRST_DAY_OF_WEEK_SEVEN = 11

		val WEEKDAYS = listOf("LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM")
	}
}
