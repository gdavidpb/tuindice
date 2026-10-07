package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import com.gdavidpb.tuindice.base.ui.text.EditableTextFieldState
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SubjectSearchTextFieldUiTest {
	@Test
	fun when_queryIsEmpty_then_displaysPlaceholderAndHidesClearButton() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectSearchTextFieldHarness(
				query = "",
				focusRequester = remember { FocusRequester() },
				onQueryChange = {},
				onClearClick = {},
				onSearch = {}
			)
		}

		assertNodeVisible(SubjectsUiTags.SearchTextField)
		onNodeWithTag(SubjectsUiTags.SearchTextField)
			.assertTextContains("Código, nombre o palabra clave")
		assertNodeHidden(SubjectsUiTags.SearchClear)
	}

	@Test
	fun when_userTypes_then_reportsTypedQueryAndDisplaysIt() = runTuIndiceUiTest {
		val reportedQueries = mutableListOf<String>()
		val query = mutableStateOf("")

		setTuIndiceTestContent {
			SubjectSearchTextFieldHarness(
				query = query.value,
				focusRequester = remember { FocusRequester() },
				onQueryChange = { value ->
					reportedQueries += value
					query.value = value
				},
				onClearClick = {},
				onSearch = {}
			)
		}

		onNodeWithTag(SubjectsUiTags.SearchTextField).performTextInput("ma11")

		assertEquals("ma11", reportedQueries.last())
		onNodeWithTag(SubjectsUiTags.SearchTextField).assertTextContains("ma11")
	}

	// The view model answers each keystroke later, off the main thread. An answer that is already
	// stale when it lands must not take back what was typed after it was sent.
	@Test
	fun when_staleEchoLandsBetweenKeystrokes_then_keepsEveryTypedCharacter() = runTuIndiceUiTest {
		val echoedQuery = mutableStateOf("")
		val reportedQueries = mutableListOf<String>()

		setTuIndiceTestContent {
			SubjectSearchTextFieldHarness(
				query = echoedQuery.value,
				focusRequester = remember { FocusRequester() },
				onQueryChange = { value -> reportedQueries += value },
				onClearClick = {},
				onSearch = {}
			)
		}

		onNodeWithTag(SubjectsUiTags.SearchTextField).performTextInput("m")
		onNodeWithTag(SubjectsUiTags.SearchTextField).performTextInput("a")

		// The answer to "m" lands only now, after "a" is already in the field.
		runOnIdle { echoedQuery.value = reportedQueries.first() }
		waitForIdle()

		onNodeWithTag(SubjectsUiTags.SearchTextField).performTextInput("c")

		reportedQueries.drop(1).forEach { reported ->
			runOnIdle { echoedQuery.value = reported }
			waitForIdle()
		}

		assertEquals("mac", reportedQueries.last())
		onNodeWithTag(SubjectsUiTags.SearchTextField).assertTextContains("mac")
	}

	// A view model that falls back to an earlier query is repeating something the field said.
	@Test
	fun when_theCallerFallsBackToAnEarlierQuery_then_keepsWhatWasTyped() = runTuIndiceUiTest {
		val echoedQuery = mutableStateOf("")
		val reportedQueries = mutableListOf<String>()

		setTuIndiceTestContent {
			SubjectSearchTextFieldHarness(
				query = echoedQuery.value,
				focusRequester = remember { FocusRequester() },
				onQueryChange = { value ->
					reportedQueries += value
					echoedQuery.value = value
				},
				onClearClick = {},
				onSearch = {}
			)
		}

		onNodeWithTag(SubjectsUiTags.SearchTextField).performTextInput("calculo")
		onNodeWithTag(SubjectsUiTags.SearchTextField).performTextInput(" ")

		runOnIdle { echoedQuery.value = "calculo" }
		waitForIdle()

		onNodeWithTag(SubjectsUiTags.SearchTextField).performTextInput("2")

		assertEquals("calculo 2", reportedQueries.last())
		onNodeWithTag(SubjectsUiTags.SearchTextField).assertTextContains("calculo 2")
	}

	@Test
	fun when_queryHasText_then_clearButtonInvokesClearCallback() = runTuIndiceUiTest {
		var clearClicks = 0
		val reportedQueries = mutableListOf<String>()

		setTuIndiceTestContent {
			SubjectSearchTextFieldHarness(
				query = "ma11",
				focusRequester = remember { FocusRequester() },
				onQueryChange = { value -> reportedQueries += value },
				onClearClick = { clearClicks++ },
				onSearch = {}
			)
		}

		onNodeWithTag(SubjectsUiTags.SearchTextField).assertTextContains("ma11")
		onNodeWithTag(SubjectsUiTags.SearchClear)
			.assertContentDescriptionEquals("Limpiar búsqueda")
			.performClick()

		assertEquals(1, clearClicks)
		assertEquals(emptyList(), reportedQueries)
	}

	@Test
	fun when_externalQueryChanges_then_fieldDisplaysTheNewQuery() = runTuIndiceUiTest {
		val query = mutableStateOf("ma11")

		setTuIndiceTestContent {
			SubjectSearchTextFieldHarness(
				query = query.value,
				focusRequester = remember { FocusRequester() },
				onQueryChange = {},
				onClearClick = {},
				onSearch = {}
			)
		}

		onNodeWithTag(SubjectsUiTags.SearchTextField).assertTextContains("ma11")

		query.value = "Física"
		onNodeWithTag(SubjectsUiTags.SearchTextField).assertTextContains("Física")

		query.value = ""
		waitForIdle()
		assertNodeHidden(SubjectsUiTags.SearchClear)
	}

	@Test
	fun when_imeSearchPerformed_then_invokesSearchCallback() = runTuIndiceUiTest {
		var searches = 0

		setTuIndiceTestContent {
			SubjectSearchTextFieldHarness(
				query = "ma11",
				focusRequester = remember { FocusRequester() },
				onQueryChange = {},
				onClearClick = {},
				onSearch = { searches++ }
			)
		}

		onNodeWithTag(SubjectsUiTags.SearchTextField).performImeAction()

		assertEquals(1, searches)
	}

	// The test plays the screen: it owns the field state and syncs it once per composition.
	@Composable
	private fun SubjectSearchTextFieldHarness(
		query: String,
		focusRequester: FocusRequester,
		onQueryChange: (String) -> Unit,
		onClearClick: () -> Unit,
		onSearch: () -> Unit
	) {
		val fieldState = remember { EditableTextFieldState(query) }

		fieldState.syncExternal(query, resetKey = null)

		SubjectSearchTextField(
			fieldState = fieldState,
			focusRequester = focusRequester,
			onQueryChange = onQueryChange,
			onClearClick = onClearClick,
			onSearch = onSearch
		)
	}
}
