package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.agentMessage
import com.gdavidpb.tuindice.assistant.ui.contentState
import com.gdavidpb.tuindice.base.ui.LocalTuIndiceCompositionProbe
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AssistantChatComposerRecompositionUiTest {
	@Test
	fun when_onlyTheComposerChanges_then_theAgentMessageIsNotComposedAgain() = runTuIndiceUiTest {
		val compositions = mutableListOf<String>()
		// Defined once: a new probe would change the local, and every message would compose for that alone.
		val probe: (String) -> Unit = { name -> compositions += name }
		val onAction: (AssistantChat.Action) -> Unit = {}
		val conversation = contentState(items = listOf(agentMessage(id = MESSAGE_ID)))
		var content by mutableStateOf(conversation)
		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceCompositionProbe provides probe) {
				AssistantChatConversationView(content = content, onAction = onAction)
			}
		}
		waitForIdle()
		assertTrue(MESSAGE_NAME in compositions, "the message was not composed at first: $compositions")

		compositions.clear()
		// The student types: the composer changes, and the conversation keeps the instance of its items.
		content = conversation.copy(composer = conversation.composer.copy(text = "hola", isSendEnabled = true))
		waitForIdle()

		assertFalse(MESSAGE_NAME in compositions, "the message was composed again while typing: $compositions")
	}

	@Test
	fun when_theMessageChanges_then_theAgentMessageIsComposedAgain() = runTuIndiceUiTest {
		val compositions = mutableListOf<String>()
		val probe: (String) -> Unit = { name -> compositions += name }
		val onAction: (AssistantChat.Action) -> Unit = {}
		var content by mutableStateOf(contentState(items = listOf(agentMessage(id = MESSAGE_ID))))
		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceCompositionProbe provides probe) {
				AssistantChatConversationView(content = content, onAction = onAction)
			}
		}
		waitForIdle()

		compositions.clear()
		content = contentState(items = listOf(agentMessage(id = MESSAGE_ID, text = "Otro texto.")))
		waitForIdle()

		assertTrue(MESSAGE_NAME in compositions, "a changed message was not composed: $compositions")
	}

	private companion object {
		const val MESSAGE_ID = "message-1"
		const val MESSAGE_NAME = "agent-message:message-1"
	}
}
