package com.gdavidpb.tuindice.subjects.ui.view

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
			SubjectSearchTextField(
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
			SubjectSearchTextField(
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

	@Test
	fun when_queryHasText_then_clearButtonInvokesClearCallback() = runTuIndiceUiTest {
		var clearClicks = 0
		val reportedQueries = mutableListOf<String>()

		setTuIndiceTestContent {
			SubjectSearchTextField(
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
			SubjectSearchTextField(
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
			SubjectSearchTextField(
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
}
