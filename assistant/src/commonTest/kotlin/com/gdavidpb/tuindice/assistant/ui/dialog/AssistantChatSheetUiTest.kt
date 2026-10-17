package com.gdavidpb.tuindice.assistant.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.agentMessage
import com.gdavidpb.tuindice.assistant.ui.contentState
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertIs

@OptIn(ExperimentalTestApi::class)
class AssistantChatSheetUiTest {
	@Test
	fun when_conversationIsOpen_then_theSheetNeverNamesTheAssistant() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatSheet(
				state = contentState(items = listOf(agentMessage())),
				onAction = {}
			)
		}

		assertNodeVisible(AssistantUiTags.ChatSheet)
		onAllNodes(hasText("TuIndice", substring = true, ignoreCase = true), useUnmergedTree = true)
			.assertCountEquals(0)
		onAllNodes(hasContentDescription("TuIndice", substring = true, ignoreCase = true), useUnmergedTree = true)
			.assertCountEquals(0)
	}

	@Test
	fun when_consentIsRequired_then_theSheetAsksForAcceptance() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatSheet(
				state = AssistantChat.State.ConsentRequired,
				onAction = { actions.add(it) }
			)
		}

		assertNodeVisible(AssistantUiTags.ConsentAccept)
		onNodeWithTag(AssistantUiTags.ConsentAccept).performClick()

		assertIs<AssistantChat.Action.AcceptConsent>(actions.single())
	}
}
