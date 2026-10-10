package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.testkit.ui.TuIndiceTestSizeClass
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PeekingSelectorViewUiTest {
	@Test
	fun when_selectedKeyMatchesAnItem_then_marksOnlyThatItemAsSelected() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermSelector(
				terms = listOf("2023", "2024", "2025"),
				selectedTerm = "2024"
			)
		}

		assertNodeVisible(termTag("2024"))
		onNodeWithTag(termTag("2024"))
			.assertIsSelected()
			.assertHasClickAction()
			.assertTextEquals("2024 seleccionado")
		// The neighbours peek at both sides and are told they are not the selection.
		onNodeWithTag(termTag("2023"))
			.assertIsDisplayed()
			.assertIsNotSelected()
			.assertTextEquals("2023")
		onNodeWithTag(termTag("2025"))
			.assertIsDisplayed()
			.assertIsNotSelected()
			.assertTextEquals("2025")
	}

	@Test
	fun when_peekingItemIsTapped_then_reportsThatItemWithoutSelectingItByItself() = runTuIndiceUiTest {
		val picked = mutableListOf<String>()

		setTuIndiceTestContent {
			TermSelector(
				terms = listOf("2023", "2024", "2025"),
				selectedTerm = "2024",
				itemWidthFraction = 0.4f,
				onTermSelected = { term -> picked += term }
			)
		}

		assertNodeVisible(termTag("2025"))
		onNodeWithTag(termTag("2025")).performClick()
		onNodeWithTag(termTag("2023")).performClick()

		assertEquals(listOf("2025", "2023"), picked)
		// Selection is the caller's state: the view keeps showing the key it was handed.
		onNodeWithTag(termTag("2024")).assertIsSelected()
		onNodeWithTag(termTag("2025")).assertIsNotSelected()
	}

	@Test
	fun when_selectedKeyChanges_then_bringsTheNewSelectionIntoView() = runTuIndiceUiTest {
		val terms = (2016..2025).map { year -> year.toString() }
		val selectedTerm = mutableStateOf("2016")

		setTuIndiceTestContent {
			TermSelector(
				terms = terms,
				selectedTerm = selectedTerm.value
			)
		}

		assertNodeVisible(termTag("2016"))
		onNodeWithTag(termTag("2016")).assertIsSelected()
		// Far items are not laid out until the selection travels to them.
		assertNodeHidden(termTag("2024"))

		runOnIdle {
			selectedTerm.value = "2024"
		}

		waitUntil(timeoutMillis = SCROLL_TIMEOUT_MILLIS) {
			onAllNodesWithTag(termTag("2024")).fetchSemanticsNodes().isNotEmpty()
		}
		waitForIdle()

		onNodeWithTag(termTag("2024"))
			.assertIsDisplayed()
			.assertIsSelected()
			.assertTextEquals("2024 seleccionado")
		assertNodeHidden(termTag("2016"))
	}

	@Test
	fun when_initialSelectionIsFarAhead_then_startsPositionedOnIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermSelector(
				terms = (2016..2025).map { year -> year.toString() },
				selectedTerm = "2022"
			)
		}

		assertNodeVisible(termTag("2022"))
		onNodeWithTag(termTag("2022")).assertIsSelected()
		assertNodeHidden(termTag("2016"))
	}

	@Test
	fun when_itemWidthFractionIsProvided_then_itemsTakeThatShareOfTheWidth() = runTuIndiceUiTest {
		val itemWidthFraction = mutableStateOf<Float?>(null)

		setTuIndiceTestContent(sizeClass = TuIndiceTestSizeClass.Compact) {
			TermSelector(
				terms = listOf("2023", "2024", "2025"),
				selectedTerm = "2024",
				itemWidthFraction = itemWidthFraction.value
			)
		}

		// Half of the 360dp viewport by default, so both neighbours can peek.
		assertNodeVisible(termTag("2024"))
		onNodeWithTag(termTag("2024")).assertWidthIsEqualTo(180.dp)

		runOnIdle {
			itemWidthFraction.value = 0.8f
		}
		waitForIdle()

		onNodeWithTag(termTag("2024")).assertWidthIsEqualTo(288.dp)
	}

	@Composable
	private fun TermSelector(
		terms: List<String>,
		selectedTerm: String?,
		itemWidthFraction: Float? = null,
		onTermSelected: (String) -> Unit = {}
	) {
		val itemContent: @Composable (String, Boolean) -> Unit = { term, isSelected ->
			Text(if (isSelected) "$term seleccionado" else term)
		}

		if (itemWidthFraction == null) {
			PeekingSelectorView(
				items = terms,
				selectedItemKey = selectedTerm,
				itemKey = { term -> term },
				onItemSelected = onTermSelected,
				itemTestTag = { term -> termTag(term) }
			) { term, isSelected ->
				itemContent(term, isSelected)
			}
		} else {
			PeekingSelectorView(
				items = terms,
				selectedItemKey = selectedTerm,
				itemKey = { term -> term },
				onItemSelected = onTermSelected,
				itemTestTag = { term -> termTag(term) },
				itemWidthFraction = itemWidthFraction
			) { term, isSelected ->
				itemContent(term, isSelected)
			}
		}
	}

	private companion object {
		const val SCROLL_TIMEOUT_MILLIS = 5_000L

		fun termTag(term: String): String = "peeking_selector_term_$term"
	}
}
