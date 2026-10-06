package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.input.ImeAction
import com.gdavidpb.tuindice.base.ui.text.EditableTextFieldState
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.performTextInputPerCharacter
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SearchTextFieldUiTest {
	@Test
	fun when_queryIsEmpty_then_showsThePlaceholderAndNothingToClear() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SearchField(initialQuery = "")
		}

		assertNodeVisible(FieldTag)
		onNodeWithTag(FieldTag).assert(hasSetTextAction())
		onNodeWithText("Código o nombre").assertIsDisplayed()
		assertNodeHidden(tag = ClearTag, useUnmergedTree = true)
	}

	@Test
	fun when_textIsTyped_then_reportsTheNewValueAndOffersToClearIt() = runTuIndiceUiTest {
		val reported = mutableListOf<String>()

		setTuIndiceTestContent {
			SearchField(
				initialQuery = "",
				onQueryChange = { query -> reported += query }
			)
		}

		onNodeWithTag(FieldTag).performTextInput("ci25")

		assertEquals("ci25", reported.last())
		onNodeWithTag(FieldTag).assert(hasText("ci25"))
		onNodeWithTag(ClearTag, useUnmergedTree = true)
			.assertIsDisplayed()
			.assertHasClickAction()
	}

	@Test
	fun when_clearButtonIsTapped_then_emptiesTheFieldAndReportsTheClear() = runTuIndiceUiTest {
		var clearClicks = 0

		setTuIndiceTestContent {
			SearchField(
				initialQuery = "matematicas",
				onClearClick = { clearClicks++ }
			)
		}

		// The icon inside the button is what a screen reader announces.
		onNodeWithTag(ClearTag).assertContentDescriptionEquals("Limpiar búsqueda")
		onNodeWithTag(ClearTag, useUnmergedTree = true).performClick()
		waitForIdle()

		assertEquals(1, clearClicks)
		onNodeWithText("Código o nombre").assertIsDisplayed()
		assertNodeHidden(tag = ClearTag, useUnmergedTree = true)
	}

	// The view model answers each keystroke later, off the main thread. An answer that is already
	// stale when it lands must not take back what was typed after it was sent.
	@Test
	fun when_staleEchoLandsBetweenKeystrokes_then_keepsEveryTypedCharacter() = runTuIndiceUiTest {
		val echoedQuery = mutableStateOf("")
		val reported = mutableListOf<String>()

		setTuIndiceTestContent {
			EchoedSearchField(echoedQuery = echoedQuery.value, onQueryChange = { query -> reported += query })
		}

		onNodeWithTag(FieldTag).performTextInput("a")
		onNodeWithTag(FieldTag).performTextInput("b")

		// The answer to "a" lands only now, after "b" is already in the field.
		runOnIdle { echoedQuery.value = reported.first() }
		waitForIdle()

		onNodeWithTag(FieldTag).performTextInput("c")

		reported.drop(1).forEach { query ->
			runOnIdle { echoedQuery.value = query }
			waitForIdle()
		}

		assertEquals("abc", reported.last())
		onNodeWithTag(FieldTag).assert(hasText("abc"))
	}

	// The stale answer lands after "b", then the answer to "b", and only then comes "c".
	@Test
	fun when_staleEchoRestoresTheTextBeforeTheNextKey_then_keepsTheTypingOrder() = runTuIndiceUiTest {
		val echoedQuery = mutableStateOf("")
		val reported = mutableListOf<String>()

		setTuIndiceTestContent {
			EchoedSearchField(echoedQuery = echoedQuery.value, onQueryChange = { query -> reported += query })
		}

		onNodeWithTag(FieldTag).performTextInput("a")
		onNodeWithTag(FieldTag).performTextInput("b")

		runOnIdle { echoedQuery.value = reported[0] }
		waitForIdle()
		runOnIdle { echoedQuery.value = reported[1] }
		waitForIdle()

		onNodeWithTag(FieldTag).performTextInput("c")

		assertEquals("abc", reported.last())
		onNodeWithTag(FieldTag).assert(hasText("abc"))
	}

	@Test
	fun when_charactersAreTypedOneByOneWithALaggingEcho_then_reportsTheQuery() = runTuIndiceUiTest {
		val input = "matematicas-i"
		val echoedQuery = mutableStateOf("")
		val reported = mutableListOf<String>()

		setTuIndiceTestContent {
			EchoedSearchField(echoedQuery = echoedQuery.value, onQueryChange = { query -> reported += query })
		}

		performTextInputPerCharacter(FieldTag, input) { index ->
			// The answer to the keystroke from two keys ago lands just before this one.
			if (index >= 2) {
				runOnIdle { echoedQuery.value = reported[index - 2] }
				waitForIdle()
			}
		}

		runOnIdle { echoedQuery.value = reported.last() }
		waitForIdle()

		assertEquals(input, reported.last())
		onNodeWithTag(FieldTag).assert(hasText(input))
	}

	@Test
	fun when_theCallerSetsATextTheUserNeverTyped_then_showsIt() = runTuIndiceUiTest {
		val echoedQuery = mutableStateOf("fisica")

		setTuIndiceTestContent {
			EchoedSearchField(echoedQuery = echoedQuery.value)
		}

		onNodeWithTag(FieldTag).assert(hasText("fisica"))

		runOnIdle { echoedQuery.value = "quimica" }
		waitForIdle()

		onNodeWithTag(FieldTag).assert(hasText("quimica"))
	}

	@Test
	fun when_keyboardSearchActionIsPerformed_then_reportsTheSearch() = runTuIndiceUiTest {
		var searches = 0

		setTuIndiceTestContent {
			SearchField(
				initialQuery = "fisica",
				onSearch = { searches++ }
			)
		}

		onNodeWithTag(FieldTag)
			.assert(SemanticsMatcher.expectValue(SemanticsProperties.ImeAction, ImeAction.Search))
			.performImeAction()

		assertEquals(1, searches)
	}

	// The test plays the screen: it hands back what was typed.
	@Composable
	private fun SearchField(
		initialQuery: String,
		onQueryChange: (String) -> Unit = {},
		onClearClick: () -> Unit = {},
		onSearch: () -> Unit = {}
	) {
		val query = remember { mutableStateOf(initialQuery) }
		val fieldState = remember { EditableTextFieldState(initialQuery) }

		fieldState.syncExternal(query.value)

		SearchTextField(
			fieldState = fieldState,
			placeholderText = "Código o nombre",
			clearContentDescription = "Limpiar búsqueda",
			onQueryChange = { newQuery ->
				query.value = newQuery
				onQueryChange(newQuery)
			},
			onClearClick = onClearClick,
			onSearch = onSearch,
			modifier = Modifier.testTag(FieldTag),
			clearButtonModifier = Modifier.testTag(ClearTag)
		)
	}

	// The test plays a view model that answers late: it decides what the field is handed.
	@Composable
	private fun EchoedSearchField(
		echoedQuery: String,
		onQueryChange: (String) -> Unit = {}
	) {
		val fieldState = remember { EditableTextFieldState(echoedQuery) }

		fieldState.syncExternal(echoedQuery)

		SearchTextField(
			fieldState = fieldState,
			placeholderText = "Código o nombre",
			clearContentDescription = "Limpiar búsqueda",
			onQueryChange = onQueryChange,
			onClearClick = {},
			onSearch = {},
			modifier = Modifier.testTag(FieldTag),
			clearButtonModifier = Modifier.testTag(ClearTag)
		)
	}

	private companion object {
		const val FieldTag = "search_text_field"
		const val ClearTag = "search_text_field_clear"
	}
}
