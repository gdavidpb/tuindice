package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.presentation.testing.agentMessage
import com.gdavidpb.tuindice.assistant.presentation.testing.assistantConversation
import com.gdavidpb.tuindice.assistant.presentation.testing.changeTextAction
import com.gdavidpb.tuindice.assistant.presentation.testing.chatTest
import com.gdavidpb.tuindice.assistant.presentation.testing.content
import com.gdavidpb.tuindice.assistant.presentation.testing.openAction
import com.gdavidpb.tuindice.chatcontract.message.Text
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class AssistantChatComposerBehaviorTest {
	@Test
	fun changeText_keepsTheItemsOfTheConversation_andPresentsOnlyTheComposer() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(agentMessage("a1", Text("Tu promedio es 15,4")))
		)
		viewModel.openAction(null)
		val before = viewModel.content()

		viewModel.changeTextAction("hola")

		val after = viewModel.content()
		assertSame(before.items, after.items, "a change of the text presents the conversation again")
		assertEquals("hola", after.composer.text)
	}
}
