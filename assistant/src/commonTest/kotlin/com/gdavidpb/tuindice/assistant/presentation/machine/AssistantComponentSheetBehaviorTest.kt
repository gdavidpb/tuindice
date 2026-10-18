package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantComponentSheet
import com.gdavidpb.tuindice.assistant.presentation.testing.agentMessage
import com.gdavidpb.tuindice.assistant.presentation.testing.assistantConversation
import com.gdavidpb.tuindice.assistant.presentation.testing.componentSheetTest
import com.gdavidpb.tuindice.assistant.presentation.testing.emptyAssistantConversation
import com.gdavidpb.tuindice.assistant.presentation.testing.openAction
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.component.UnknownChatComponent
import com.gdavidpb.tuindice.chatcontract.message.Component
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

// The component sheet through its view model: it shows the component the store holds, follows it, and closes when it
// leaves.
class AssistantComponentSheetBehaviorTest {
	private val component = UnknownChatComponent(id = "component-1", fallbackText = "Resumen de tu semestre")

	private val conversationWithComponent = assistantConversation(
		items = listOf(agentMessage("message-1", Component(component = component)))
	)

	@Test
	fun when_theComponentIsInTheConversation_then_theSheetShowsIt() = componentSheetTest(
		initial = conversationWithComponent
	) { _, viewModel ->
		viewModel.openAction(messageId = "message-1", componentId = "component-1")

		val content = assertIs<AssistantComponentSheet.State.Content>(viewModel.state.value)
		assertEquals("message-1", content.messageId)
		assertEquals(component, content.component)
	}

	@Test
	fun when_theComponentIsNotInTheConversation_then_theSheetCloses() = componentSheetTest(
		initial = emptyAssistantConversation()
	) { fixture, viewModel ->
		viewModel.openAction(messageId = "message-1", componentId = "component-1")

		assertEquals(AssistantComponentSheet.State.Missing, viewModel.state.value)
		assertTrue(fixture.effects.any { effect -> effect is AssistantComponentSheet.Effect.Dismiss })
	}

	@Test
	fun when_theComponentLeavesTheConversation_then_theSheetCloses() = componentSheetTest(
		initial = conversationWithComponent
	) { fixture, viewModel ->
		viewModel.openAction(messageId = "message-1", componentId = "component-1")
		assertIs<AssistantComponentSheet.State.Content>(viewModel.state.value)

		fixture.conversation.conversation.value = emptyAssistantConversation()

		assertEquals(AssistantComponentSheet.State.Missing, viewModel.state.value)
		assertTrue(fixture.effects.any { effect -> effect is AssistantComponentSheet.Effect.Dismiss })
	}

	@Test
	fun when_aLinkIsTapped_then_theSheetGoesToItsTarget_andLeavesTheClosingToTheJump() = componentSheetTest(
		initial = conversationWithComponent
	) { fixture, viewModel ->
		viewModel.openAction(messageId = "message-1", componentId = "component-1")

		viewModel.dispatch(
			AssistantComponentSheet.Action.OpenNavTarget(target = NavTarget.SUMMARY, entityRefs = emptyList())
		)

		val navigation = fixture.effects.filterIsInstance<AssistantComponentSheet.Effect.NavigateTo>().single()
		assertEquals(NavTarget.SUMMARY, navigation.target)
		assertTrue(fixture.effects.none { effect -> effect is AssistantComponentSheet.Effect.Dismiss })
	}
}
