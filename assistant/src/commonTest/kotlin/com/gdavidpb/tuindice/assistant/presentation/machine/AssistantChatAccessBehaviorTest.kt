package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantRequestRefusedException
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatNoticeItem
import com.gdavidpb.tuindice.assistant.presentation.testing.acceptConsentAction
import com.gdavidpb.tuindice.assistant.presentation.testing.agentMessage
import com.gdavidpb.tuindice.assistant.presentation.testing.assistantConversation
import com.gdavidpb.tuindice.assistant.presentation.testing.changeTextAction
import com.gdavidpb.tuindice.assistant.presentation.testing.chatTest
import com.gdavidpb.tuindice.assistant.presentation.testing.content
import com.gdavidpb.tuindice.assistant.presentation.testing.declineConsentAction
import com.gdavidpb.tuindice.assistant.presentation.testing.openAction
import com.gdavidpb.tuindice.assistant.presentation.testing.pickSuggestionAction
import com.gdavidpb.tuindice.assistant.presentation.testing.sendMessageAction
import com.gdavidpb.tuindice.assistant.testing.assistantStatus
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantAvailability
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode
import com.gdavidpb.tuindice.chatcontract.consent.ConsentStatus
import com.gdavidpb.tuindice.chatcontract.error.AssistantErrorCodes
import com.gdavidpb.tuindice.chatcontract.intent.IntentOutcomeReport
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.message.SuggestedReply
import com.gdavidpb.tuindice.chatcontract.message.Suggestions
import com.gdavidpb.tuindice.chatcontract.message.Text
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
	fun open_reconcilesTheConsentWithTheStatusOfTheServer() = chatTest { fixture, viewModel ->
		fixture.standing.answer = assistantStatus().copy(consent = ConsentStatus(version = 2, accepted = true))

		viewModel.openAction(null)

		assertTrue(viewModel.state.value is AssistantChat.State.Content)
		assertEquals(listOf(ConsentStatus(version = 2, accepted = true)), fixture.consent.reconciled)
	}

	@Test
	fun aRefusedAcceptanceOfConsent_showsItsError_andAcceptingAgainCanSucceed() = chatTest { fixture, viewModel ->
		fixture.consent.failure = IllegalStateException("refused")
		viewModel.openAction(null)
		assertEquals(AssistantChat.State.ConsentRequired, viewModel.state.value)

		viewModel.acceptConsentAction()

		assertEquals(AssistantChat.State.ConsentFailed, viewModel.state.value)

		fixture.consent.failure = null
		viewModel.acceptConsentAction()

		assertTrue(viewModel.state.value is AssistantChat.State.Content)
	}

	@Test
	fun open_sendsTheOutcomesOfTheIntentsTheServerHasNotTakenYet() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.stageOutcome(IntentOutcomeReport(intentId = "i9", outcome = OutcomeKind.CANCELLED))

		viewModel.openAction(null)

		assertEquals(listOf("i9"), fixture.intents.reports.map { report -> report.intentId })
		assertTrue(fixture.conversation.pendingOutcomes().isEmpty())
	}

	@Test
	fun declineConsent_closesTheSheet() = chatTest { fixture, viewModel ->
		viewModel.openAction(null)

		viewModel.declineConsentAction()

		assertEquals(listOf<AssistantChat.Effect>(AssistantChat.Effect.Dismiss), fixture.effects)
	}

	@Test
	fun open_readsTheModeTheServerGivesWithTheStatus() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.standing.answer = assistantStatus().copy(mode = "guided")

		viewModel.openAction(null)

		assertTrue(viewModel.content().isGuided)
	}

	@Test
	fun guidedOnly_readsTheModeFromTheServer_andAnOnModeReopensTheComposer() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.standing.answer = assistantStatus().copy(mode = "on")
		fixture.turns.sendError = AssistantRequestRefusedException(AssistantErrorCodes.GUIDED_ONLY, null, "guided")
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")

		viewModel.sendMessageAction()

		val content = viewModel.content()
		assertFalse(content.isGuided)
		assertTrue(content.composer.isEnabled)
		assertEquals("hola", content.composer.text)
	}

	@Test
	fun aGuidedLock_holdsThroughAnAcceptedTurn_andAnOnModeReleasesIt() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.standing.failure = IllegalStateException("status unavailable")
		fixture.turns.sendError = AssistantRequestRefusedException(AssistantErrorCodes.GUIDED_ONLY, null, "guided")
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(
				agentMessage(
					"a1",
					Text("¿Quieres ver tu promedio?"),
					Suggestions(listOf(SuggestedReply(id = "s1", label = "Sí", text = "Sí, quiero verlo")))
				)
			)
		)
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()
		assertTrue(viewModel.content().isGuided)

		fixture.turns.sendError = null
		viewModel.pickSuggestionAction("s1")
		assertTrue(viewModel.content().isGuided)

		fixture.availability.availability.value = AssistantAvailability(mode = AssistantMode.GUIDED, serverDisabled = false)
		fixture.availability.availability.value = AssistantAvailability(mode = AssistantMode.ON, serverDisabled = false)
		assertFalse(viewModel.content().isGuided)
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
