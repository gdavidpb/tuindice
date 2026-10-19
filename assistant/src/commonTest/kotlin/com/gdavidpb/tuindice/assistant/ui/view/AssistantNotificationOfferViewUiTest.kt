package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
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
class AssistantNotificationOfferViewUiTest {
	@Test
	fun when_theOfferShows_then_itsTwoAnswersAreVisible() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantNotificationOfferView(onAction = {})
		}

		assertNodeVisible(AssistantUiTags.NotificationOffer)
		assertNodeVisible(AssistantUiTags.NotificationOfferLater)
		assertNodeVisible(AssistantUiTags.NotificationOfferActivate)
	}

	@Test
	fun when_activateIsTapped_then_activateNotificationsIsDispatched() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantNotificationOfferView(onAction = { actions.add(it) })
		}

		onNodeWithTag(AssistantUiTags.NotificationOfferActivate).performClick()

		assertIs<AssistantChat.Action.ActivateNotifications>(actions.single())
	}

	@Test
	fun when_laterIsTapped_then_dismissNotificationOfferIsDispatched() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantNotificationOfferView(onAction = { actions.add(it) })
		}

		onNodeWithTag(AssistantUiTags.NotificationOfferLater).performClick()

		assertIs<AssistantChat.Action.DismissNotificationOffer>(actions.single())
	}

	@Test
	fun when_theContentHasNoOffer_then_theConversationShowsNoCard() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatConversationView(content = emptyContent(notificationOffer = false), onAction = {})
		}

		onNodeWithTag(AssistantUiTags.NotificationOffer).assertDoesNotExist()
	}

	@Test
	fun when_theContentHasTheOffer_then_theConversationShowsTheCard() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatConversationView(content = emptyContent(notificationOffer = true), onAction = {})
		}

		assertNodeVisible(AssistantUiTags.NotificationOffer)
	}

	private fun emptyContent(notificationOffer: Boolean) = AssistantChat.State.Content(
		context = null,
		items = emptyList(),
		progressLabel = null,
		composer = ChatComposerItem(text = "", isEnabled = true, isSendEnabled = false),
		suggestions = emptyList(),
		notice = null,
		isGuided = false,
		notificationOffer = notificationOffer
	)
}
