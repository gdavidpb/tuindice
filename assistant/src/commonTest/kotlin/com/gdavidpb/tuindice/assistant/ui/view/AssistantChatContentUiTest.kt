package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.AssistantIntentOutcome
import com.gdavidpb.tuindice.assistant.presentation.model.ChatComposerItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatPartItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatSuggestionItem
import com.gdavidpb.tuindice.assistant.presentation.model.IntentCardStatus
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.INTENT_ID
import com.gdavidpb.tuindice.assistant.ui.agentMessage
import com.gdavidpb.tuindice.assistant.ui.contentState
import com.gdavidpb.tuindice.assistant.ui.contextChip
import com.gdavidpb.tuindice.assistant.ui.intentCard
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

	@Test
	fun when_aPhotoIsBeingRead_then_theProgressShowsAndThePhotoButtonIsOff() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatContent(
				state = contentState(
					composer = ChatComposerItem(text = "", isEnabled = true, isSendEnabled = false, isPhotoEnabled = false)
				).copy(isExtracting = true),
				onAction = {},
				isCameraAvailable = true,
				modifier = Modifier.fillMaxSize()
			)
		}

		assertNodeVisible(AssistantUiTags.ProgressLabel)
		assertNodeDisabled(AssistantUiTags.PhotoButton)
	}

	@Test
	fun when_theComposerIsOff_then_thePhotoButtonIsNotShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatContent(
				state = contentState(
					composer = ChatComposerItem(text = "", isEnabled = false, isSendEnabled = false)
				),
				onAction = {},
				isCameraAvailable = true,
				modifier = Modifier.fillMaxSize()
			)
		}

		assertNodeHidden(AssistantUiTags.PhotoButton)
	}

	@Test
	fun when_aPhotoIsReadIntoTheChat_then_itsIntentCardIsShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatContent(
				state = contentState(
					items = listOf(
						ChatItem.Agent(
							id = "photo-1",
							parts = listOf(
								ChatPartItem.Text("Encontré 1 evaluación."),
								ChatPartItem.IntentCard(intentCard())
							),
							useful = null,
							reported = false,
							copyText = "Encontré 1 evaluación."
						)
					)
				),
				onAction = {},
				modifier = Modifier.fillMaxSize()
			)
		}

		assertNodeVisible(AssistantUiTags.Intent.card(INTENT_ID))
	}

	@Test
	fun when_theLastProposalIsTallerThanTheList_then_itsButtonsAreScrolledIntoView() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatContent(
				state = contentState(
					items = listOf(
						ChatItem.Agent(
							id = "proposal-1",
							parts = listOf(
								ChatPartItem.Text(LONG_TEXT),
								ChatPartItem.IntentCard(intentCard())
							),
							useful = null,
							reported = false,
							copyText = LONG_TEXT
						)
					)
				),
				onAction = {},
				modifier = Modifier.height(CONTAINER_HEIGHT)
			)
		}

		assertNodeVisible(AssistantUiTags.Intent.confirm(INTENT_ID))
	}

	@Test
	fun when_theLastMessageIsLongAndHasNoProposal_then_itsStartIsShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatContent(
				state = contentState(items = listOf(agentMessage(text = FIRST_TEXT + " " + LONG_TEXT))),
				onAction = {},
				modifier = Modifier.height(CONTAINER_HEIGHT)
			)
		}

		onNodeWithText(FIRST_TEXT, substring = true).assertIsDisplayed()
	}

	@Test
	fun when_theLastProposalIsResolvedAndTaller_then_itsOutcomeIsScrolledIntoView() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatContent(
				state = contentState(
					items = listOf(
						ChatItem.Agent(
							id = "proposal-1",
							parts = listOf(
								ChatPartItem.Text(LONG_TEXT),
								ChatPartItem.IntentCard(
									intentCard(status = IntentCardStatus.Resolved(AssistantIntentOutcome.CONFIRMED))
								)
							),
							useful = null,
							reported = false,
							copyText = LONG_TEXT
						)
					)
				),
				onAction = {},
				modifier = Modifier.height(CONTAINER_HEIGHT)
			)
		}

		assertNodeVisible(AssistantUiTags.Intent.receipt(INTENT_ID))
	}

	private companion object {
		val CONTAINER_HEIGHT = 300.dp
		const val FIRST_TEXT = "Encontré 3 evaluaciones."
		val LONG_TEXT = "Revisa cada una antes de confirmar, porque se agregan a tu historial de evaluaciones. ".repeat(20)
	}
}
