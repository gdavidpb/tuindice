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
import androidx.compose.ui.text.input.TextFieldValue
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
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
	fun when_clearButtonIsTapped_then_reportsTheClearAndLeavesTheValueToTheCaller() = runTuIndiceUiTest {
		var clearClicks = 0

		setTuIndiceTestContent {
			SearchField(
				initialQuery = "matematicas",
				onClearClick = { clearClicks++ }
			)
		}

		onNodeWithTag(ClearTag, useUnmergedTree = true).performClick()

		assertEquals(1, clearClicks)
		// The icon inside the button is what a screen reader announces.
		onNodeWithTag(ClearTag).assertContentDescriptionEquals("Limpiar búsqueda")
		// The field is stateless: it keeps showing the value it is handed.
		onNodeWithTag(FieldTag).assert(hasText("matematicas"))
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

	// The field is stateless: the test plays the screen, handing back what was typed.
	@Composable
	private fun SearchField(
		initialQuery: String,
		onQueryChange: (String) -> Unit = {},
		onClearClick: () -> Unit = {},
		onSearch: () -> Unit = {}
	) {
		val value = remember { mutableStateOf(TextFieldValue(initialQuery)) }

		SearchTextField(
			value = value.value,
			placeholderText = "Código o nombre",
			clearContentDescription = "Limpiar búsqueda",
			onValueChange = { newValue ->
				value.value = newValue
				onQueryChange(newValue.text)
			},
			onClearClick = onClearClick,
			onSearch = onSearch,
			modifier = Modifier.testTag(FieldTag),
			clearButtonModifier = Modifier.testTag(ClearTag)
		)
	}

	private companion object {
		const val FieldTag = "search_text_field"
		const val ClearTag = "search_text_field_clear"
	}
}
