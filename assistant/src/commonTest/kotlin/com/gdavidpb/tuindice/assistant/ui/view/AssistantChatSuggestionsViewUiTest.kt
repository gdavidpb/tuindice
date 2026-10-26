package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatSuggestionItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AssistantChatSuggestionsViewUiTest {
	@Test
	fun when_suggestionsAreShown_then_eachSitsUnderTheLastAtTheFullWidth_andATapPicksIt() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatSuggestionsView(suggestions = SUGGESTIONS, onAction = actions::add)
		}

		val bounds = SUGGESTIONS.map { suggestion ->
			onNodeWithTag(AssistantUiTags.Chat.suggestion(suggestion.id)).fetchSemanticsNode().boundsInRoot
		}
		bounds.zipWithNext().forEach { (above, below) ->
			assertTrue(above.bottom <= below.top)
			assertEquals(above.left, below.left)
			assertEquals(above.width, below.width)
		}
		onNodeWithTag(AssistantUiTags.Chat.suggestion(SUGGESTIONS.last().id)).performClick()

		runOnIdle {
			assertEquals(SUGGESTIONS.last().id, assertIs<AssistantChat.Action.PickSuggestion>(actions.single()).suggestionId)
		}
	}

	@Test
	fun when_suggestionsAreShown_then_nothingSitsAboveTheFirst() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatSuggestionsView(
				suggestions = SUGGESTIONS,
				onAction = {},
				modifier = Modifier.testTag(LIST_TAG)
			)
		}

		val list = onNodeWithTag(LIST_TAG).fetchSemanticsNode().boundsInRoot
		val first = onNodeWithTag(AssistantUiTags.Chat.suggestion(SUGGESTIONS.first().id)).fetchSemanticsNode().boundsInRoot

		assertEquals(list.top, first.top)
	}

	private companion object {
		const val LIST_TAG = "suggestions"
		val SUGGESTIONS = listOf(
			ChatSuggestionItem(id = "sq.record.last", label = "¿Cómo me fue en el último trimestre?"),
			ChatSuggestionItem(id = "sq.record.simulate", label = "Simula mis notas de este trimestre"),
			ChatSuggestionItem(id = "sq.record.goal", label = "¿Qué índice necesito para llegar a mi meta?")
		)
	}
}
