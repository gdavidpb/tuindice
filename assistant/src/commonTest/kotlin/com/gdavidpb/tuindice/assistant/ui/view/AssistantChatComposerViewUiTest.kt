package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatComposerItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertIs

@OptIn(ExperimentalTestApi::class)
class AssistantChatComposerViewUiTest {
	@Test
	fun when_aTurnCanBeStopped_then_theStopButtonTakesTheSendPlace_andStoppingCancelsTheTurn() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatComposerView(
				composer = ChatComposerItem(text = "", isEnabled = true, isSendEnabled = false, isStoppable = true),
				isCameraAvailable = false,
				onAction = { actions.add(it) }
			)
		}

		assertNodeVisible(AssistantUiTags.StopButton)
		onNodeWithContentDescription(STOP_DESCRIPTION).assertIsDisplayed()
		onNodeWithTag(AssistantUiTags.SendButton).assertDoesNotExist()

		onNodeWithTag(AssistantUiTags.StopButton).performClick()

		assertIs<AssistantChat.Action.CancelTurn>(actions.single())
	}

	@Test
	fun when_noTurnCanBeStopped_then_theSendButtonIsThere_andNoStopButton() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatComposerView(
				composer = ChatComposerItem(text = "hola", isEnabled = true, isSendEnabled = true),
				isCameraAvailable = false,
				onAction = { actions.add(it) }
			)
		}

		assertNodeVisible(AssistantUiTags.SendButton)
		onNodeWithTag(AssistantUiTags.StopButton).assertDoesNotExist()

		onNodeWithTag(AssistantUiTags.SendButton).performClick()

		assertIs<AssistantChat.Action.Send>(actions.single())
	}

	private companion object {
		const val STOP_DESCRIPTION = "Detener respuesta"
	}
}
