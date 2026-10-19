package com.gdavidpb.tuindice.assistant.presentation.viewmodel

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantComponentSheet
import com.gdavidpb.tuindice.assistant.presentation.testing.AssistantComponentSheetFixture
import com.gdavidpb.tuindice.assistant.presentation.testing.agentMessage
import com.gdavidpb.tuindice.assistant.presentation.testing.assistantConversation
import com.gdavidpb.tuindice.assistant.presentation.testing.componentSheetTest
import com.gdavidpb.tuindice.assistant.presentation.testing.openAction
import com.gdavidpb.tuindice.chatcontract.component.UnknownChatComponent
import com.gdavidpb.tuindice.chatcontract.message.Component
import com.gdavidpb.tuindice.testkit.coroutines.withUnconfinedTestDispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AssistantComponentSheetViewModelContractTest {
	@Test
	fun initialState_isIdle_untilTheSheetOpens() = runTest {
		withUnconfinedTestDispatchers { dispatchers ->
			val viewModel = AssistantComponentSheetFixture().viewModel(dispatchers)

			assertEquals(AssistantComponentSheet.State.Idle, viewModel.state.value)
		}
	}

	@Test
	fun when_theSheetOpens_then_itShowsTheComponentNamed() = componentSheetTest(
		initial = assistantConversation(
			items = listOf(
				agentMessage("message-1", Component(component = UnknownChatComponent(id = "component-1", fallbackText = "x")))
			)
		)
	) { _, viewModel ->
		viewModel.openAction(messageId = "message-1", componentId = "component-1")

		val content = assertIs<AssistantComponentSheet.State.Content>(viewModel.state.value)
		assertEquals("message-1", content.messageId)
	}
}
