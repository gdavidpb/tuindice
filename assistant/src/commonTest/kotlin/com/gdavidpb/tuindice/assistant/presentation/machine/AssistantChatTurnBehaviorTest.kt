package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantRequestRefusedException
import com.gdavidpb.tuindice.assistant.domain.model.AssistantActiveTurn
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversationItem
import com.gdavidpb.tuindice.assistant.domain.model.AssistantInputLimits
import com.gdavidpb.tuindice.assistant.domain.model.AssistantTurnPhase
import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserMessage
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatNoticeItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatPartItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatSuggestionItem
import com.gdavidpb.tuindice.assistant.presentation.testing.agentMessage
import com.gdavidpb.tuindice.assistant.presentation.testing.assistantConversation
import com.gdavidpb.tuindice.assistant.presentation.testing.bubbles
import com.gdavidpb.tuindice.assistant.presentation.testing.cancelTurnAction
import com.gdavidpb.tuindice.assistant.presentation.testing.changeTextAction
import com.gdavidpb.tuindice.assistant.presentation.testing.chatTest
import com.gdavidpb.tuindice.assistant.presentation.testing.content
import com.gdavidpb.tuindice.assistant.presentation.testing.openAction
import com.gdavidpb.tuindice.assistant.presentation.testing.pickSuggestionAction
import com.gdavidpb.tuindice.assistant.presentation.testing.removeContextAction
import com.gdavidpb.tuindice.assistant.presentation.testing.retryTurnAction
import com.gdavidpb.tuindice.assistant.presentation.testing.sendMessageAction
import com.gdavidpb.tuindice.assistant.testing.assistantStatus
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseStanding
import com.gdavidpb.tuindice.chatcontract.abuse.StandingLevel
import com.gdavidpb.tuindice.chatcontract.error.AssistantErrorCodes
import com.gdavidpb.tuindice.chatcontract.event.RunError
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode
import com.gdavidpb.tuindice.chatcontract.event.RunFinished
import com.gdavidpb.tuindice.chatcontract.message.SuggestedReply
import com.gdavidpb.tuindice.chatcontract.message.Suggestions
import com.gdavidpb.tuindice.chatcontract.message.Text
import com.gdavidpb.tuindice.chatcontract.turn.TurnAccepted
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_chat_restricted_cooldown
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AssistantChatTurnBehaviorTest {
	@Test
	fun send_startsTheTurnWithItsContext_clearsTheDraft_andFollowsTheTurn() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t1", conversationId = "c1", lastSeq = 4)
		fixture.turns.events = listOf(RunFinished(seq = 5, turnId = "t1"))
		val context = AssistantScreenContext(AssistantScreen.EVALUATIONS, visibleFields = mapOf("subject" to "CI4325"))
		viewModel.openAction(context)

		viewModel.changeTextAction("  ¿Cuál es mi promedio?  ")
		viewModel.sendMessageAction()

		val request = fixture.turns.requests.single()
		assertEquals("¿Cuál es mi promedio?", request.message.text)
		assertEquals(mapOf("subject" to "CI4325"), request.screenContext?.visibleFields)
		assertEquals(listOf("t1" to 4L), fixture.turns.followed)
		assertEquals("", viewModel.content().composer.text)
		assertNull(viewModel.content().notice)
	}

	@Test
	fun quotaExhausted_marksTheMessageFailed_keepsTheText_andSaysWhenItReturns() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.sendError = AssistantRequestRefusedException(AssistantErrorCodes.QUOTA_EXHAUSTED, 90, "quota")
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")

		viewModel.sendMessageAction()

		val content = viewModel.content()
		assertEquals(ChatNoticeItem.QuotaExhausted(message = "quota:90"), content.notice)
		assertEquals(listOf(true), content.bubbles().map { bubble -> bubble.isFailed })
		assertEquals("hola", content.composer.text)
	}

	@Test
	fun theStudentsEdits_keepTheTextRevision_andTheSendThatClearsTheText_movesIt() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t1", conversationId = "c1", lastSeq = 0)
		viewModel.openAction(null)
		val opened = viewModel.content().composer.textRevision

		viewModel.changeTextAction("hola")
		assertEquals(opened, viewModel.content().composer.textRevision)

		viewModel.sendMessageAction()
		val cleared = viewModel.content().composer

		assertEquals("", cleared.text)
		assertTrue(cleared.textRevision > opened)
	}

	@Test
	fun aRefusedSend_givesTheTextBack_underANewTextRevision() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.sendError = AssistantRequestRefusedException(AssistantErrorCodes.QUOTA_EXHAUSTED, 90, "quota")
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		val typed = viewModel.content().composer.textRevision

		viewModel.sendMessageAction()
		val restored = viewModel.content().composer

		// The send cleared it and the refusal gave it back: two replacements, so the field adopts «hola» again even
		// though the student typed it before.
		assertEquals("hola", restored.text)
		assertEquals(typed + 2, restored.textRevision)
	}

	@Test
	fun otherFailure_offersARetry_andRetryResendsTheFailedText() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.sendError = AssistantRequestRefusedException("unknown", null, "unknown")
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()
		assertEquals(ChatNoticeItem.TurnFailed(message = "send-failed", canRetry = true), viewModel.content().notice)

		fixture.turns.sendError = null
		viewModel.retryTurnAction()

		assertEquals(listOf("hola", "hola"), fixture.turns.requests.map { request -> request.message.text })
		assertNull(viewModel.content().notice)
	}

	@Test
	fun retryOfAFailedSend_resendsTheSameClientTurnId_andLeavesOneBubble() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.sendError = AssistantRequestRefusedException("unknown", null, "unknown")
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()

		fixture.turns.sendError = null
		viewModel.retryTurnAction()

		val ids = fixture.turns.requests.map { request -> request.clientTurnId }
		assertEquals(2, ids.size)
		assertEquals(ids.first(), ids.last())
		assertEquals(listOf(false), viewModel.content().bubbles().map { bubble -> bubble.isFailed })
	}

	@Test
	fun anUnconfirmedTurn_offersARetryThatResendsTheSameClientTurnId() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(
				AssistantConversationItem.User(
					AssistantUserMessage(id = "client-1", text = "hola", createdAtMillis = 1L)
				)
			),
			activeTurn = AssistantActiveTurn(
				turnId = "t1",
				lastSeq = 3,
				progressLabel = null,
				status = AssistantTurnPhase.UNCONFIRMED
			)
		)
		viewModel.openAction(null)

		assertEquals(ChatNoticeItem.TurnFailed(message = "failure:TRANSIENT", canRetry = true), viewModel.content().notice)

		viewModel.retryTurnAction()

		val request = fixture.turns.requests.single()
		assertEquals("client-1", request.clientTurnId)
		assertEquals("hola", request.message.text)
	}

	@Test
	fun aRunErrorKeepsTheSuggestionOfTheTurn_forItsRetry() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(
				agentMessage(
					"a1",
					Text("¿Quieres ver tu promedio?"),
					Suggestions(listOf(SuggestedReply(id = "s1", label = "Sí", text = "Sí, quiero verlo")))
				)
			)
		)
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t1", conversationId = "c1", lastSeq = 0)
		fixture.turns.events = listOf(RunError(seq = 1, turnId = "t1", code = RunErrorCode.TRANSIENT, retryable = true))
		viewModel.openAction(null)

		viewModel.pickSuggestionAction("s1")
		viewModel.retryTurnAction()

		assertEquals(listOf("s1", "s1"), fixture.turns.requests.map { request -> request.message.suggestionId })
	}

	@Test
	fun anOtherRunErrorThatCannotBeRetried_saysSoWithoutInvitingARetry() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.events = listOf(RunError(seq = 1, turnId = "t1", code = RunErrorCode.OTHER, retryable = false))
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")

		viewModel.sendMessageAction()

		assertEquals(ChatNoticeItem.TurnFailed(message = "failure:OTHER:final", canRetry = false), viewModel.content().notice)
	}

	@Test
	fun guidedOnly_onSend_keepsTheStudentsDraftInTheComposer() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.sendError = AssistantRequestRefusedException(AssistantErrorCodes.GUIDED_ONLY, null, "guided")
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")

		viewModel.sendMessageAction()

		assertEquals("hola", viewModel.content().composer.text)
	}

	@Test
	fun aSentTurnWithoutItsFirstEvent_isSending_andTheSendAndTheSuggestionsWait() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t1", conversationId = "c1", lastSeq = 0)
		fixture.turns.holdEvents = true
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

		val content = viewModel.content()
		assertTrue(content.isSending)
		assertFalse(content.composer.isSendEnabled)
		assertFalse(content.composer.isPhotoEnabled)
		assertTrue(content.suggestions.isEmpty())
	}

	@Test
	fun theFirstEventOfTheSentTurn_endsTheSending_andTheTurnCanBeStopped() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t1", conversationId = "c1", lastSeq = 0)
		fixture.turns.holdEvents = true
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()
		assertTrue(viewModel.content().isSending)

		fixture.conversation.conversation.value = assistantConversation(
			activeTurn = AssistantActiveTurn(
				turnId = "t1",
				lastSeq = 1,
				progressLabel = null,
				status = AssistantTurnPhase.RUNNING
			)
		)

		val content = viewModel.content()
		assertFalse(content.isSending)
		assertTrue(content.composer.isStoppable)
	}

	@Test
	fun stopIsOfferedWhileATurnRuns_andStoppingItAsksTheServerToCancelIt() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(
			activeTurn = AssistantActiveTurn(
				turnId = "t1",
				lastSeq = 3,
				progressLabel = "Revisando tu pensum…",
				status = AssistantTurnPhase.RUNNING
			)
		)
		viewModel.openAction(null)
		assertTrue(viewModel.content().composer.isStoppable)

		viewModel.cancelTurnAction()

		assertEquals(listOf("t1"), fixture.turns.cancelled)
	}

	@Test
	fun stopIsNotOfferedWhenNoTurnRuns_andCancellingDoesNothing() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(null)
		assertFalse(viewModel.content().composer.isStoppable)

		viewModel.cancelTurnAction()

		assertTrue(fixture.turns.cancelled.isEmpty())
	}

	@Test
	fun turnInProgress_keepsTheDraft_andShowsNoNotice() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.sendError = AssistantRequestRefusedException(AssistantErrorCodes.TURN_IN_PROGRESS, null, "busy")
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")

		viewModel.sendMessageAction()

		val content = viewModel.content()
		assertNull(content.notice)
		assertEquals("hola", content.composer.text)
		assertTrue(content.bubbles().single().isFailed)
	}

	@Test
	fun guidedOnly_onSend_pausesTheComposer() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.standing.answer = assistantStatus().copy(mode = "guided")
		fixture.turns.sendError = AssistantRequestRefusedException(AssistantErrorCodes.GUIDED_ONLY, null, "guided")
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")

		viewModel.sendMessageAction()

		val content = viewModel.content()
		assertTrue(content.isGuided)
		assertFalse(content.composer.isEnabled)
		assertEquals(ChatNoticeItem.Guided(message = "guided"), content.notice)
	}

	@Test
	fun suspension_onSend_pausesTheComposer_andOffersTheAppealWhenTheStandingAllowsIt() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.standing.answer = assistantStatus().copy(
			standing = AbuseStanding(level = StandingLevel.COOLDOWN, appealable = true)
		)
		fixture.turns.sendError = AssistantRequestRefusedException(AssistantErrorCodes.SUSPENDED, null, "suspended")
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")

		viewModel.sendMessageAction()

		val content = viewModel.content()
		assertEquals(
			ChatNoticeItem.Restricted(
				message = UiText.Resource(Res.string.assistant_chat_restricted_cooldown),
				canAppeal = true
			),
			content.notice
		)
		assertFalse(content.composer.isEnabled)
	}

	@Test
	fun assistantDisabled_onSend_closesTheSheet() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.sendError = AssistantRequestRefusedException(AssistantErrorCodes.ASSISTANT_DISABLED, 60, "off")
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")

		viewModel.sendMessageAction()

		assertEquals(AssistantChat.State.Unavailable, viewModel.state.value)
		assertEquals(listOf<AssistantChat.Effect>(AssistantChat.Effect.Dismiss), fixture.effects)
	}

	@Test
	fun consentRequired_onSend_asksForConsentAgain() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.sendError = AssistantRequestRefusedException(AssistantErrorCodes.CONSENT_REQUIRED, null, "consent")
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")

		viewModel.sendMessageAction()

		assertEquals(AssistantChat.State.ConsentRequired, viewModel.state.value)
	}

	@Test
	fun messageTooLong_isNotSent_andKeepsTheTextInTheComposer() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(null)
		val tooLong = "a".repeat(AssistantInputLimits.MESSAGE_MAX_LENGTH + 1)
		viewModel.changeTextAction(tooLong)

		viewModel.sendMessageAction()

		val content = viewModel.content()
		assertEquals(ChatNoticeItem.TurnFailed(message = "too-long:2000", canRetry = false), content.notice)
		assertEquals(tooLong, content.composer.text)
		assertTrue(fixture.turns.requests.isEmpty())
		assertTrue(content.bubbles().isEmpty())
	}

	@Test
	fun runError_isShownWithItsRetryFlag() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.events = listOf(RunError(seq = 1, turnId = "t1", code = RunErrorCode.TRANSIENT, retryable = true))
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")

		viewModel.sendMessageAction()

		assertEquals(ChatNoticeItem.TurnFailed(message = "failure:TRANSIENT", canRetry = true), viewModel.content().notice)
		assertTrue(viewModel.content().bubbles().single().isFailed)
	}

	@Test
	fun nonRetryableRunError_offersNoRetry() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.events = listOf(RunError(seq = 1, turnId = "t1", code = RunErrorCode.REFUSED, retryable = false))
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")

		viewModel.sendMessageAction()

		assertEquals(ChatNoticeItem.TurnFailed(message = "failure:REFUSED", canRetry = false), viewModel.content().notice)
	}

	@Test
	fun suggestionsOfTheLastAgentMessage_areOffered_andSentWithTheirText() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
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

		val content = viewModel.content()
		assertEquals(listOf(ChatSuggestionItem(id = "s1", label = "Sí")), content.suggestions)
		val agent = content.items.single() as ChatItem.Agent
		assertEquals(listOf<ChatPartItem>(ChatPartItem.Text("¿Quieres ver tu promedio?")), agent.parts)

		viewModel.pickSuggestionAction("s1")

		val request = fixture.turns.requests.single()
		assertEquals("Sí, quiero verlo", request.message.text)
		assertEquals("s1", request.message.suggestionId)
		assertEquals("Sí, quiero verlo", viewModel.content().bubbles().single().text)
	}

	@Test
	fun progressLabel_isTheLabelOfTheRunningTurn_andClearsWhenItEnds() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(
			activeTurn = AssistantActiveTurn(
				turnId = "t1",
				lastSeq = 3,
				progressLabel = "Revisando tu pensum…",
				status = AssistantTurnPhase.RUNNING
			)
		)
		viewModel.openAction(null)

		assertEquals("Revisando tu pensum…", viewModel.content().progressLabel)

		fixture.conversation.conversation.value = assistantConversation(
			activeTurn = AssistantActiveTurn(
				turnId = "t1",
				lastSeq = 4,
				progressLabel = null,
				status = AssistantTurnPhase.FINISHED
			)
		)

		assertNull(viewModel.content().progressLabel)
	}

	@Test
	fun removingTheContext_sendsTheTurnWithoutIt() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(AssistantScreenContext(AssistantScreen.RECORD))

		viewModel.removeContextAction()
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()

		assertNull(viewModel.content().context)
		assertNull(fixture.turns.requests.single().screenContext)
	}
}
