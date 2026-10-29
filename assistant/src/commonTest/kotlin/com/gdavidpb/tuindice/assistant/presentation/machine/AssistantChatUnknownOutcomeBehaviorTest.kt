package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatPartItem
import com.gdavidpb.tuindice.assistant.presentation.model.IntentCardStatus
import com.gdavidpb.tuindice.assistant.presentation.testing.AssistantChatFixture
import com.gdavidpb.tuindice.assistant.presentation.testing.agentMessage
import com.gdavidpb.tuindice.assistant.presentation.testing.assistantConversation
import com.gdavidpb.tuindice.assistant.presentation.testing.chatTest
import com.gdavidpb.tuindice.assistant.presentation.testing.content
import com.gdavidpb.tuindice.assistant.presentation.testing.intentCard
import com.gdavidpb.tuindice.assistant.presentation.testing.openAction
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantChatViewModel
import com.gdavidpb.tuindice.assistant.testing.assistantIntent
import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.intent.RememberArgs
import com.gdavidpb.tuindice.chatcontract.message.IntentPart
import com.gdavidpb.tuindice.chatcontract.message.MessagePart
import com.gdavidpb.tuindice.chatcontract.message.UnknownMessagePart
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

// A card resolved with an outcome this build does not know is closed: it keeps what it proposed, with no actions and no
// outcome label, and its message shows one notice of a part this build cannot show.
class AssistantChatUnknownOutcomeBehaviorTest {

	private val intent: Intent = assistantIntent(
		id = "i1",
		args = RememberArgs(statement = "Trabajo de noche", category = "rutina", quote = "de noche")
	)

	@Test
	fun aCardResolvedWithAnUnknownOutcome_isClosed() = chatTest { fixture, viewModel ->
		openWith(fixture, viewModel, IntentPart(intent), UnknownMessagePart)

		assertEquals(IntentCardStatus.Closed, viewModel.content().intentCard("i1").status)
	}

	@Test
	fun theMessageOfAClosedCard_showsOneNotice() = chatTest { fixture, viewModel ->
		openWith(fixture, viewModel, IntentPart(intent), UnknownMessagePart)

		val notices = viewModel.agentParts("a1").filter { part -> part == ChatPartItem.Text(NOTICE) }

		assertEquals(1, notices.size)
	}

	private suspend fun openWith(
		fixture: AssistantChatFixture,
		viewModel: AssistantChatViewModel,
		vararg parts: MessagePart
	) {
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(agentMessage("a1", *parts)))
			.copy(unknownOutcomeIntents = setOf("i1"))
		viewModel.openAction(null)
	}

	private fun AssistantChatViewModel.agentParts(id: String): List<ChatPartItem> {
		return assertIs<ChatItem.Agent>(content().items.single { item -> item.id == id }).parts
	}

	private companion object {
		// The notice the fake texts give (see FakeAssistantChatTexts).
		const val NOTICE = "part-not-shown"
	}
}
