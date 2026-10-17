package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatComposerItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatSuggestionItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.contentState
import com.gdavidpb.tuindice.assistant.ui.contextChip
import com.gdavidpb.tuindice.testkit.ui.assertNodeDisabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeEnabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalTestApi::class)
class AssistantChatContentUiTest {
	@Test
	fun when_chatIsGuided_then_hidesTheComposerAndOffersOnlySuggestions() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatContent(
				state = contentState(
					suggestions = listOf(ChatSuggestionItem(id = "suggestion-1", label = "Ver mi pensum")),
					isGuided = true
				),
				onAction = { actions.add(it) },
				modifier = Modifier.fillMaxSize()
			)
		}

		assertNodeHidden(AssistantUiTags.Composer)
		assertNodeVisible(AssistantUiTags.Chat.suggestion("suggestion-1"))
		onNodeWithTag(AssistantUiTags.Chat.suggestion("suggestion-1")).performClick()

		assertEquals("suggestion-1", assertIs<AssistantChat.Action.PickSuggestion>(actions.single()).suggestionId)
	}

	@Test
	fun when_contextChipIsRemoved_then_dispatchesRemoveContext() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatContent(
				state = contentState(context = contextChip()),
				onAction = { actions.add(it) },
				modifier = Modifier.fillMaxSize()
			)
		}

		assertNodeVisible(AssistantUiTags.ContextChip)
		onNodeWithTag(AssistantUiTags.ContextRemove).performClick()

		assertIs<AssistantChat.Action.RemoveContext>(actions.single())
	}

	@Test
	fun when_composerIsSendable_then_sendDispatchesSend() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatContent(
				state = contentState(
					composer = ChatComposerItem(text = "Hola", isEnabled = true, isSendEnabled = true)
				),
				onAction = { actions.add(it) },
				modifier = Modifier.fillMaxSize()
			)
		}

		assertNodeEnabled(AssistantUiTags.SendButton)
		onNodeWithTag(AssistantUiTags.SendButton).performClick()

		assertIs<AssistantChat.Action.Send>(actions.single())
	}

	@Test
	fun when_composerCannotSend_then_sendButtonIsDisabled() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatContent(
				state = contentState(
					composer = ChatComposerItem(text = "", isEnabled = true, isSendEnabled = false)
				),
				onAction = {},
				modifier = Modifier.fillMaxSize()
			)
		}

		assertNodeVisible(AssistantUiTags.SendButton)
		assertNodeDisabled(AssistantUiTags.SendButton)
	}
}
