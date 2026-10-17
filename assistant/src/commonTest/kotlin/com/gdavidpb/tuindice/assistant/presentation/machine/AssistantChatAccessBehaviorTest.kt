package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatNoticeItem
import com.gdavidpb.tuindice.assistant.presentation.testing.acceptConsentAction
import com.gdavidpb.tuindice.assistant.presentation.testing.chatTest
import com.gdavidpb.tuindice.assistant.presentation.testing.content
import com.gdavidpb.tuindice.assistant.presentation.testing.declineConsentAction
import com.gdavidpb.tuindice.assistant.presentation.testing.openAction
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantAvailability
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AssistantChatAccessBehaviorTest {
	@Test
	fun open_whileTheAssistantIsOff_closesTheSheet() = chatTest { fixture, viewModel ->
		fixture.availability.availability.value = AssistantAvailability(mode = AssistantMode.OFF, serverDisabled = false)

		viewModel.openAction(null)

		assertEquals(AssistantChat.State.Unavailable, viewModel.state.value)
		assertEquals(listOf<AssistantChat.Effect>(AssistantChat.Effect.Dismiss), fixture.effects)
	}

	@Test
	fun assistantGoingOffMidChat_closesTheSheet() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(null)
		assertTrue(viewModel.state.value is AssistantChat.State.Content)

		fixture.availability.availability.value = AssistantAvailability(mode = AssistantMode.ON, serverDisabled = true)

		assertEquals(AssistantChat.State.Unavailable, viewModel.state.value)
		assertEquals(listOf<AssistantChat.Effect>(AssistantChat.Effect.Dismiss), fixture.effects)
	}

	@Test
	fun open_withoutConsent_asksForIt_andAcceptingShowsTheContent() = chatTest { fixture, viewModel ->
		viewModel.openAction(null)
		assertEquals(AssistantChat.State.ConsentRequired, viewModel.state.value)

		viewModel.acceptConsentAction()

		assertTrue(viewModel.state.value is AssistantChat.State.Content)
		assertEquals(listOf(true), fixture.consent.changes)
	}

	@Test
	fun declineConsent_closesTheSheet() = chatTest { fixture, viewModel ->
		viewModel.openAction(null)

		viewModel.declineConsentAction()

		assertEquals(listOf<AssistantChat.Effect>(AssistantChat.Effect.Dismiss), fixture.effects)
	}

	@Test
	fun open_inGuidedMode_pausesTheComposer_andSaysSo() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.availability.availability.value = AssistantAvailability(mode = AssistantMode.GUIDED, serverDisabled = false)

		viewModel.openAction(null)

		val content = viewModel.content()
		assertTrue(content.isGuided)
		assertFalse(content.composer.isEnabled)
		assertEquals(ChatNoticeItem.Guided(message = "guided"), content.notice)
	}
}
