package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.presentation.model.ChatFallbackItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatPartItem
import com.gdavidpb.tuindice.assistant.presentation.testing.AssistantChatFixture
import com.gdavidpb.tuindice.assistant.presentation.testing.agentMessage
import com.gdavidpb.tuindice.assistant.presentation.testing.assistantConversation
import com.gdavidpb.tuindice.assistant.presentation.testing.chatTest
import com.gdavidpb.tuindice.assistant.presentation.testing.content
import com.gdavidpb.tuindice.assistant.presentation.testing.openAction
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantChatViewModel
import com.gdavidpb.tuindice.chatcontract.component.UnknownChatComponent
import com.gdavidpb.tuindice.chatcontract.message.Component
import com.gdavidpb.tuindice.chatcontract.message.MessagePart
import com.gdavidpb.tuindice.chatcontract.message.Text
import com.gdavidpb.tuindice.chatcontract.message.UnknownMessagePart
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

// A part of an answer that this build cannot show in full, because it carries a value the contract does not know, is
// left out, or shown as the fallback text of its component. The rest of the message stays, and one notice closes it.
class AssistantChatUnknownPartBehaviorTest {

	@Test
	fun aMessageWithAnUnknownPart_showsItsRest_andOneNotice() = chatTest { fixture, viewModel ->
		openWithMessage(
			fixture,
			viewModel,
			Text("Hola"),
			UnknownMessagePart,
			Component(UnknownChatComponent("c1", "Respaldo")),
			Text("Fin")
		)

		val parts = viewModel.agentParts("a1")

		assertEquals(ChatPartItem.Text("Hola"), parts[0])
		assertEquals("Respaldo", assertIs<ChatFallbackItem>(assertIs<ChatPartItem.Component>(parts[1]).item).fallbackText)
		assertEquals(ChatPartItem.Text("Fin"), parts[2])
		assertEquals(listOf(ChatPartItem.Text(NOTICE)), parts.drop(3))
	}

	@Test
	fun aMessageWithSeveralUnknownParts_hasOneNotice() = chatTest { fixture, viewModel ->
		openWithMessage(fixture, viewModel, UnknownMessagePart, Text("Hola"), UnknownMessagePart)

		assertEquals(listOf(ChatPartItem.Text("Hola"), ChatPartItem.Text(NOTICE)), viewModel.agentParts("a1"))
	}

	@Test
	fun aMessageWithoutUnknownParts_hasNoNotice() = chatTest { fixture, viewModel ->
		openWithMessage(fixture, viewModel, Text("Hola"), Text("Fin"))

		assertEquals(listOf(ChatPartItem.Text("Hola"), ChatPartItem.Text("Fin")), viewModel.agentParts("a1"))
	}

	private suspend fun openWithMessage(
		fixture: AssistantChatFixture,
		viewModel: AssistantChatViewModel,
		vararg parts: MessagePart
	) {
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(agentMessage("a1", *parts)))
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
