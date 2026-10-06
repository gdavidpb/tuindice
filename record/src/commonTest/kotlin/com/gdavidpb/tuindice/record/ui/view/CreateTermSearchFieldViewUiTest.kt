package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.input.TextFieldValue
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class CreateTermSearchFieldViewUiTest {
	@Test
	fun when_nothingIsTyped_then_thePlaceholderSaysWhatToSearchBy_andThereIsNothingToClear() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SearchField(initialQuery = "")
		}

		assertNodeVisible(RecordUiTags.CreateSyntheticTermSearchField)
		onNodeWithText("Código, nombre o palabra clave").assertIsDisplayed()
		assertNodeHidden(RecordUiTags.CreateSyntheticTermSearchClearButton)
	}

	@Test
	fun when_theStudentTypes_then_theQueryIsReported_andTheClearButtonAppears() = runTuIndiceUiTest {
		val reported = mutableListOf<String>()

		setTuIndiceTestContent {
			SearchField(initialQuery = "", onQueryChange = { query -> reported += query })
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchField).performTextInput("ma11")

		assertEquals("ma11", reported.last())
		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchField).assert(hasText("ma11"))
		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchClearButton).assertIsDisplayed()
	}

	@Test
	fun when_clearIsTapped_then_itIsReported_andTheButtonIsReadAsClearingTheSearch() = runTuIndiceUiTest {
		var clearClicks = 0

		setTuIndiceTestContent {
			SearchField(initialQuery = "fisica", onClearQueryClick = { clearClicks++ })
		}

		// The cross carries no label on screen: the description is what names it aloud.
		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchClearButton)
			.assertContentDescriptionEquals("Limpiar búsqueda")
			.performClick()

		assertEquals(1, clearClicks)
		// Clearing is the caller's to do: the field shows what it is handed.
		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchField).assert(hasText("fisica"))
	}

	@Test
	fun when_theKeyboardsSearchKeyIsPressed_then_theSearchIsReported() = runTuIndiceUiTest {
		var searches = 0

		setTuIndiceTestContent {
			SearchField(initialQuery = "algoritmos", onSearch = { searches++ })
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchField).performImeAction()

		assertEquals(1, searches)
	}

	// The field is stateless: the test plays the screen, handing back what was typed.
	@Composable
	private fun SearchField(
		initialQuery: String,
		onQueryChange: (String) -> Unit = {},
		onClearQueryClick: () -> Unit = {},
		onSearch: () -> Unit = {}
	) {
		val query = remember { mutableStateOf(TextFieldValue(initialQuery)) }

		CreateTermSearchField(
			query = query.value,
			focusRequester = remember { FocusRequester() },
			onQueryChange = { value ->
				query.value = value
				onQueryChange(value.text)
			},
			onClearQueryClick = onClearQueryClick,
			onSearch = onSearch
		)
	}
}
